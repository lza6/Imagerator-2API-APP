#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI Imagerator (com.techinfortainment.imagerator) 2API demo server
=================================================================
逆向来源: Ai Image to Video Generator_3_rd.apk (重打包版, pairip+SignatureKiller)
上游:     https://techinfortainment.com/ai-generation/*
协议:     明文 JSON / Multipart, 鉴权仅 device_id (android_id) — 无签名无加密

OpenAI 兼容端点:
  GET  /v1/models
  POST /v1/images/generations      {model, prompt, size, n}
  POST /v1/images/edits            multipart(image, prompt, model)
  POST /v1/video/generations       {model, prompt, size}   (OpenAI Sora 风格扩展)
  GET  /v1/video/generations/{id}  轮询视频结果
  POST /v1/chat/completions        {model:"imagerator-img", messages:[...]} → 返回图片 markdown

原生端点:
  POST /api/register               {device_id?}  → 注册新设备拿 credits
  GET  /api/credits?device_id=     → 查询余额
  GET  /api/pricing                → 价格表
  GET  /health

启动:  python demo_server.py  (端口 8902)
说明:  对上游只做单次真实调用验证过协议; 请勿对上游高频压测 (第三方生产服务)。
"""
import base64
import json
import re
import threading
import time
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import urllib.request
import urllib.error
import ssl

UPSTREAM = "https://techinfortainment.com/ai-generation"
APP_NAME = "imagerator"
PORT = 8902

# 允许自动注册的设备池 (demo 用, 每设备注册送 20~30 credits)
# 自动化号池: [device_id, credits, registered_at]
# 上游 /users/register 无鉴权无验证, 每个 device_id 送 20~30 credits, 耗尽自动注册新号
_device_pool = []
_pool_lock = threading.Lock()

_CTX = ssl.create_default_context()
_CTX.check_hostname = False
_CTX.verify_mode = ssl.CERT_NONE

JOBS = {}  # video job_id -> dict

# text2image 模型映射 (APK H.c(): Flux→Flux1schnell, 默认 ZImageTurbo_INT8)
IMAGE_MODELS = {
    "imagerator-turbo": "ZImageTurbo_INT8",
    "imagerator-flux": "Flux1schnell",
    "imagerator-nanobanana": "NanoBanana",
}
VIDEO_MODELS = {
    "imagerator-t2v": "Ltxv_13B_0_9_8_Distilled_FP8",
}


def upstream_request(path, data=None, multipart=None, headers=None, timeout=90):
    url = path if path.startswith("http") else UPSTREAM + path
    hdrs = {"Accept": "application/json"}
    if headers:
        hdrs.update(headers)
    if multipart is not None:
        boundary = "----aiv2api" + uuid.uuid4().hex
        body = b""
        for k, v in multipart.get("fields", {}).items():
            body += f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"\r\n\r\n{v}\r\n".encode()
        if "file" in multipart:
            fname, fdata = multipart["file"]
            body += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"image\"; "
                     f"filename=\"{fname}\"\r\nContent-Type: application/octet-stream\r\n\r\n").encode()
            body += fdata + b"\r\n"
        body += f"--{boundary}--\r\n".encode()
        hdrs["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        req = urllib.request.Request(url, data=body, headers=hdrs, method="POST")
    elif data is not None:
        hdrs["Content-Type"] = "application/json; charset=utf-8"
        req = urllib.request.Request(url, data=json.dumps(data).encode(), headers=hdrs, method="POST")
    else:
        req = urllib.request.Request(url, headers=hdrs)
    resp = urllib.request.urlopen(req, context=_CTX, timeout=timeout)
    return json.loads(resp.read().decode())


def _register_new():
    """注册一个新设备号 (上游送 20~30 credits, plan=Ads)。"""
    dev = uuid.uuid4().hex
    r = upstream_request("/users/register", data={
        "device_id": dev, "app_name": APP_NAME,
        "user_type": "Organic", "purchase_token": "", "product_id": ""})
    credits = int(r.get("credits", 0))
    with _pool_lock:
        _device_pool.append([dev, credits, time.time()])
    print(f"[pool] +registered {dev[:8]}… credits={credits} (pool={len(_device_pool)})", flush=True)
    _save_pool()
    return dev


def _sync_credits(dev):
    """向上游同步某设备真实余额 (本地计数可能偏低), 返回 credits 或 None。"""
    try:
        r = upstream_request(f"/users/{dev}/credits?app_name={APP_NAME}")
        c = int(r.get("credits", 0))
        with _pool_lock:
            for row in _device_pool:
                if row[0] == dev:
                    row[1] = c
        return c
    except Exception:
        return None


def get_device(min_credits=1):
    """自动化号池: 取有余额设备 → 无则同步真实余额 → 再无则注册新号。并发安全。"""
    with _pool_lock:
        for row in _device_pool:
            if row[1] >= min_credits:
                return row[0]
    # 池内全欠费 → 先向上游同步真实余额 (可能本地计数偏低)
    with _pool_lock:
        stale = [row[0] for row in _device_pool if row[1] < min_credits]
    for dev in stale[:5]:
        c = _sync_credits(dev)
        if c is not None and c >= min_credits:
            return dev
    # 还不行 → 注册新号 (最多重试 3 次)
    for _ in range(3):
        try:
            return _register_new()
        except Exception as e:
            print("[pool] register failed:", e, flush=True)
            time.sleep(1)
    raise RuntimeError("device pool exhausted: register failed 3x")


def deduct(dev, n=1):
    with _pool_lock:
        for row in _device_pool:
            if row[0] == dev:
                row[1] = max(0, row[1] - n)
    _save_pool()


def poll_result(request_id, timeout=180, interval=3.0, want="image"):
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            r = upstream_request(f"/result?request_id={request_id}&app_name={APP_NAME}")
        except Exception as e:
            return {"status": "failed", "error": str(e)}
        st = r.get("status")
        if st in ("completed", "failed", "expired", "blocked"):
            return r
        time.sleep(interval)
    return {"status": "expired", "message": "poll timeout"}


def parse_size(size, default="720x1280"):
    try:
        w, h = size.lower().split("x")
        return int(w), int(h)
    except Exception:
        w, h = default.split("x")
        return int(w), int(h)


POOL_FILE = "device_pool.json"


def _save_pool():
    try:
        with _pool_lock:
            with open(POOL_FILE, "w") as f:
                json.dump(_device_pool, f)
    except Exception as e:
        print("[pool] save failed:", e, flush=True)


def _load_pool():
    global _device_pool
    try:
        with open(POOL_FILE) as f:
            _device_pool = [row if len(row) == 3 else [row[0], row[1], time.time()] for row in json.load(f)]
        print(f"[pool] loaded {len(_device_pool)} devices", flush=True)
    except Exception:
        pass


class Handler(BaseHTTPRequestHandler):
    server_version = "aiv2api/1.0"

    def log_message(self, fmt, *args):
        print(f"[{time.strftime('%H:%M:%S')}] {self.address_string()} {fmt % args}")

    # ---------- helpers ----------
    def _send(self, code, obj, content_type="application/json; charset=utf-8"):
        body = json.dumps(obj, ensure_ascii=False).encode() if not isinstance(obj, bytes) else obj
        self.send_response(code)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _body(self):
        n = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(n) if n else b"{}"
        try:
            return json.loads(raw)
        except Exception:
            return {}

    # ---------- routes ----------
    def do_GET(self):
        path = self.path.split("?")[0]
        qs = self.path.split("?")[1] if "?" in self.path else ""

        if path == "/" or path == "/demo.html":
            with open("demo.html", "rb") as f:
                return self._send(200, f.read(), "text/html; charset=utf-8")

        if path == "/health":
            with _pool_lock:
                total = sum(r[1] for r in _device_pool)
                return self._send(200, {"ok": True, "upstream": UPSTREAM,
                                        "pool": len(_device_pool),
                                        "pool_total_credits": total,
                                        "devices": [{"device_id": r[0], "credits": r[1]} for r in _device_pool]})

        if path == "/v1/models":
            return self._send(200, {"object": "list", "data": [
                {"id": m, "object": "model", "owned_by": "imagerator-2api"}
                for m in list(IMAGE_MODELS) + list(VIDEO_MODELS) + ["imagerator-img"]]})

        if path == "/api/pricing":
            return self._send(200, upstream_request("/admin/pricing"))

        if path == "/api/credits":
            dev = dict(p.split("=") for p in qs.split("&")).get("device_id")
            if not dev:
                return self._send(400, {"error": "device_id required"})
            r = upstream_request(f"/users/{dev}/credits?app_name={APP_NAME}")
            return self._send(200, r)

        # video job polling
        m = re.match(r"^/v1/video/generations/([\w-]+)$", path)
        if m:
            job = JOBS.get(m.group(1))
            if not job:
                return self._send(404, {"error": "job not found"})
            if job["status"] == "pending" and job.get("thread") and not job["thread"].is_alive():
                job["status"] = job.get("final", {}).get("status", "failed")
            return self._send(200, {
                "id": job["id"], "status": job["status"],
                "video_url": job.get("video_url"),
                "progress": 100 if job["status"] == "completed" else 50})

        return self._send(404, {"error": f"unknown path {path}"})

    def do_POST(self):
        path = self.path.split("?")[0]
        ctype = self.headers.get("Content-Type", "")

        # ---- native register ----
        if path == "/api/register":
            b = self._body()
            dev = b.get("device_id") or uuid.uuid4().hex
            r = upstream_request("/users/register", data={
                "device_id": dev, "app_name": APP_NAME,
                "user_type": b.get("user_type", "Organic"),
                "purchase_token": "", "product_id": ""})
            with _pool_lock:
                _device_pool.append([dev, int(r.get("credits", 0))])
            return self._send(200, r)

        # ---- OpenAI images ----
        if path == "/v1/images/generations":
            b = self._body()
            model = b.get("model", "imagerator-turbo")
            prompt = b.get("prompt", "")
            if not prompt:
                return self._send(400, {"error": "prompt required"})
            w, h = parse_size(b.get("size", "720x1280"))
            dev = get_device()
            try:
                r = upstream_request("/generate/text2image", data={
                    "prompt": prompt, "original_prompt": prompt,
                    "model": IMAGE_MODELS.get(model, "ZImageTurbo_INT8"),
                    "width": w, "height": h, "steps": 8, "guidance": 7.5,
                    "negative_prompt": "", "device_id": dev,
                    "user_type": "Organic", "version": "3", "app_name": APP_NAME})
            except Exception as e:
                return self._send(502, {"error": f"upstream: {e}"})
            res = poll_result(r["request_id"])
            deduct(dev)
            if res.get("status") != "completed":
                return self._send(502, {"error": res})
            url = res.get("image_url")
            data = []
            if b.get("response_format") == "b64_json" and url:
                img = urllib.request.urlopen(urllib.request.Request(url), context=_CTX, timeout=60).read()
                data.append({"b64_json": base64.b64encode(img).decode()})
            else:
                data.append({"url": url})
            return self._send(200, {"created": int(time.time()), "data": data})

        # ---- OpenAI images edits (image2image / image2video) ----
        if path == "/v1/images/edits":
            raw = self.rfile.read(int(self.headers.get("Content-Length") or 0))
            boundary = re.search(r"boundary=([^;]+)", ctype)
            if not boundary:
                return self._send(400, {"error": "multipart required"})
            fields, ffile = {}, None
            for part in raw.split(("--" + boundary.group(1)).encode())[1:-1]:
                if b'name="' not in part:
                    continue
                name = part.split(b'name="')[1].split(b'"')[0].decode()
                val = part.split(b"\r\n\r\n", 1)[1].rsplit(b"\r\n", 1)[0]
                if name == "image":
                    ffile = val
                else:
                    fields[name] = val.decode()
            model = fields.get("model", "imagerator-i2v")
            prompt = fields.get("prompt", "")
            if not ffile:
                return self._send(400, {"error": "image file required"})
            dev = get_device()
            if "t2v" in model or "video" in model:
                mp = {"file": ("upload.png", ffile), "fields": {
                    "prompt": prompt, "original_prompt": prompt,
                    "model": "Ltxv_13B_0_9_8_Distilled_FP8", "width": "720", "height": "1280",
                    "frames": "120", "fps": "30", "steps": "1", "negative_prompt": "blur",
                    "device_id": dev, "user_type": "Organic", "version": "3", "app_name": APP_NAME}}
                ep = "/generate/image2video"
            else:
                mp = {"file": ("upload.png", ffile), "fields": {
                    "prompt": prompt, "original_prompt": prompt,
                    "model": "Flux_2_Klein_4B_BF16", "width": "720", "height": "1280",
                    "device_id": dev, "user_type": "Organic", "version": "3", "app_name": APP_NAME}}
                ep = "/generate/image2image"
            try:
                r = upstream_request(ep, multipart=mp)
            except Exception as e:
                return self._send(502, {"error": f"upstream: {e}"})
            res = poll_result(r["request_id"], timeout=300, want="video" if "video" in ep else "image")
            deduct(dev)
            if res.get("status") != "completed":
                return self._send(502, {"error": res})
            if "video" in ep:
                return self._send(200, {"status": "completed", "video_url": res.get("video_url")})
            return self._send(200, {"created": int(time.time()), "data": [{"url": res.get("image_url")}]})

        # ---- video generation (async job) ----
        if path == "/v1/video/generations":
            b = self._body()
            prompt = b.get("prompt", "")
            if not prompt:
                return self._send(400, {"error": "prompt required"})
            w, h = parse_size(b.get("size", "720x1280"))
            job_id = "vjob-" + uuid.uuid4().hex[:24]
            dev = get_device()

            def run():
                try:
                    r = upstream_request("/generate/text2video", data={
                        "prompt": prompt, "original_prompt": prompt,
                        "model": "Ltxv_13B_0_9_8_Distilled_FP8",
                        "width": w, "height": h, "frames": 120,
                        "device_id": dev, "user_type": "Organic",
                        "version": "3", "app_name": APP_NAME})
                    res = poll_result(r["request_id"], timeout=900, interval=5.0)
                    deduct(dev, 2)
                    job["final"] = res
                    if res.get("status") == "completed":
                        job["video_url"] = res.get("video_url")
                        job["status"] = "completed"
                    else:
                        job["status"] = "failed"
                except Exception as e:
                    job["status"] = "failed"
                    job["final"] = {"error": str(e)}

            job = {"id": job_id, "status": "pending", "thread": None}
            JOBS[job_id] = job
            t = threading.Thread(target=run, daemon=True)
            job["thread"] = t
            t.start()
            return self._send(200, {"id": job_id, "status": "pending"})

        # ---- chat completions → 返回图片 markdown ----
        if path == "/v1/chat/completions":
            b = self._body()
            msgs = b.get("messages", [])
            prompt = (msgs[-1].get("content", "") if msgs else "").strip()
            if not prompt:
                return self._send(400, {"error": "messages with non-empty content required"})
            dev = get_device()
            try:
                r = upstream_request("/generate/text2image", data={
                    "prompt": prompt, "original_prompt": prompt,
                    "model": "ZImageTurbo_INT8", "width": 720, "height": 1280,
                    "steps": 8, "guidance": 7.5, "negative_prompt": "",
                    "device_id": dev, "user_type": "Organic",
                    "version": "3", "app_name": APP_NAME})
            except Exception as e:
                return self._send(502, {"error": f"upstream: {e}"})
            res = poll_result(r["request_id"])
            deduct(dev)
            if res.get("status") != "completed":
                return self._send(502, {"error": res})
            cid = "chatcmpl-" + uuid.uuid4().hex[:12]
            content = f"![generated]({res['image_url']})"
            return self._send(200, {
                "id": cid, "object": "chat.completion", "created": int(time.time()),
                "model": b.get("model", "imagerator-img"),
                "choices": [{"index": 0, "message": {"role": "assistant", "content": content},
                             "finish_reason": "stop"}],
                "usage": {"prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0}})

        return self._send(404, {"error": f"unknown path {path}"})


if __name__ == "__main__":
    print(f"aiv2api demo server → upstream {UPSTREAM}, port {PORT}")
    # 启动时加载持久化号池, 空则预注册 1 个设备
    _load_pool()
    try:
        get_device()
        print(f"device pool ready: {len(_device_pool)} devices, "
              f"total credits {sum(r[1] for r in _device_pool)}")
    except Exception as e:
        print("warmup failed:", e)
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
