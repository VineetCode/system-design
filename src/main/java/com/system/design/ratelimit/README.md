# ✅ What is Rate Limiting?
It is controlling how many requests a client (user, IP, token) can make in a given time window.

👉 Helps protect backend resources from abuse or overload.

# ✅ Why is Rate Limiting needed?
Prevent API abuse (ex: bot making 1000s of requests)

Ensure fair usage (all users get fair share of resources)

Protect DB / Cache / Service from being overwhelmed

Prevent DoS attacks

Save cost (APIs like OpenAI, AWS, etc)

Manage 3rd-party quotas (if you’re calling 3rd-party APIs)

# ✅ What is a typical rule?
Per IP → "Allow 5 requests per minute per IP"

Per User → "Allow 1000 requests/day per user"

Per API Key → "Allow 500 requests/hour per API key"

# ✅ Where to put Rate Limiting?
| Level             | Example                              |
| ----------------- | ------------------------------------ |
| Client-side       | Mobile app limits user button clicks |
| CDN/Edge          | Cloudflare / Akamai edge rate limits |
| API Gateway       | AWS API Gateway / Kong / NGINX       |
| Application Layer | Code-level rate limit                |
| Database Layer    | Rare (usually not here)              |

Best → API Gateway or App layer.

# ✅ Algorithms used
| Algorithm                | Description                         |
| ------------------------ | ----------------------------------- |
| Fixed Window             | Count requests per window (per min) |
| Sliding Window           | Smooth version of fixed window      |
| Token Bucket             | Allow bursts, refill over time      |
| Leaky Bucket             | Enforce constant flow               |
| Distributed Token Bucket | Rate limit across multiple servers  |

# ✅ Example in System Design Question
👉 Suppose interviewer asks:
"Design an API for uploading images. Millions of users. How will you rate-limit?"

## You should answer:
### 1️⃣ Where to Rate Limit?

CDN level → basic IP protection

API Gateway level → rate limit per API key/user

Application layer → for finer control (VIP users)


### #️⃣ How to implement?

In-memory for small services → simple Map (not distributed)

Redis-based for distributed rate limiting

All servers check Redis for counts

Redis TTL for window expiration

Atomic INCR / DECR operations

### 3️⃣ Algorithm choice?
| Algorithm      | When to use                                           |
| -------------- | ----------------------------------------------------- |
| Fixed Window   | Very simple cases                                     |
| Sliding Window | Smooth limits                                         |
| Token Bucket   | **Most flexible** (allow bursts, used in Stripe, AWS) |
| Leaky Bucket   | **Steady flow** (traffic shaping)                     |

Production API → Token Bucket with Redis backend

## ✅ Redis-based Rate Limiter
Each IP/user → key in Redis
Example: rate_limit:user123:minute

Redis TTL = window size

Use atomic INCR — if > limit → reject (429)

## ✅ Real-world Example
Stripe API Rate Limit:

100 requests per second → per API key

If exceeded → 429 Too Many Requests

Retry-After header is returned

## ✅ How to answer in interview?
When interviewer asks "How will you Rate Limit?", say:

🗣️
👉 I’ll use Token Bucket algorithm → allows bursts, refill tokens over time
👉 Use Redis-based distributed limiter → supports horizontal scaling
👉 Each server checks Redis before serving request
👉 For VIP users → custom limits
👉 Use Retry-After header to help clients know when to retry

## ✅ Example Redis Lua Script (advanced)

local current
current = redis.call("INCR", KEYS[1])
if tonumber(current) == 1 then
   redis.call("EXPIRE", KEYS[1], ARGV[1])
end
if tonumber(current) > tonumber(ARGV[2]) then
  return 0
else 
  return 1
end

## ✅ Summary
When interviewer asks about Rate Limiting:

✅ Mention use-cases (abuse, fairness, DoS)
✅ Mention where (CDN, API Gateway, App layer)
✅ Mention algorithms (Token Bucket, Sliding Window)
✅ For distributed → use Redis
✅ Mention VIP users, Retry-After header


Here is a full ready-to-use System Design interview answer to:
# "How would you design a Rate Limiter?"

## 1️⃣ What is Rate Limiting?
👉 Rate limiting controls the number of requests a client (IP, user, API key) can send to an API in a defined time window.
It protects services from abuse, ensures fairness, avoids system overload, and prevents Denial-of-Service (DoS) attacks.

## 2️⃣ Where to apply Rate Limiting?
CDN Level → e.g., Cloudflare → stop attacks at edge

API Gateway Level → e.g., AWS API Gateway / Kong

Application Layer → fine-grained control

Per Endpoint → e.g., login endpoint stricter

👉 In my design, I would implement it at both API Gateway and Application Layer.

## 3️⃣ Key Requirements
✅ Support multiple users (millions of users)
✅ Support different limits per user (normal vs VIP)
✅ Should work in distributed system
✅ Low latency — should not slow API
✅ Resilient if cache/db is down
✅ Show proper 429 response with Retry-After header

## 4️⃣ Algorithm to use?
I would choose Token Bucket — because:
| Algorithm      | Why?                                                                |
| -------------- | ------------------------------------------------------------------- |
| Token Bucket   | Allows bursts, refill smoothly, most flexible (used by Stripe, AWS) |
| Leaky Bucket   | Enforces steady rate — smoothing                                    |
| Fixed Window   | Simple but bursty (not accurate)                                    |
| Sliding Window | Smoother than fixed, but more memory                                |

## 5️⃣ Storage?
For distributed rate limiting, we need shared state across servers → Redis is ideal.
| Storage Option | Why?                            |
| -------------- | ------------------------------- |
| In-memory      | Simple, but not distributed     |
| Redis          | Fast, atomic ops, supports TTL  |
| DB (SQL/NoSQL) | Too slow for per-request checks |

👉 So I’d use Redis, with keys like:
rate_limit:{user_id} or rate_limit:{ip}

## 6️⃣ How to implement?
Token Bucket Logic:

Each client (IP/user) has a token bucket:

Capacity = max tokens (limit)

Refill rate = tokens/sec

When a request comes in:

If bucket has token → consume token → allow

Else → return 429 Too Many Requests

How tokens refill?
On each request, compute:

tokens_to_add = (current_time - last_updated_time) * refill_rate

## 7️⃣ How to make atomic in Redis?

Use Lua script in Redis:

INCR token count

EXPIRE key

All atomic → avoid race condition

Example Redis Lua:

local tokens = redis.call('GET', KEYS[1])
if tokens == false then
tokens = ARGV[1]
end
if tonumber(tokens) > 0 then
redis.call('DECR', KEYS[1])
return 1
else
return 0
end

## 8️⃣ What response?
On 429, return:

HTTP 429 Too Many Requests

Retry-After header → tells client when to retry

## 9️⃣ Example limits

# 10️⃣ Scaling
Redis cluster for horizontal scaling

Can shard Redis by user ID hash

Use connection pool to Redis

## Final Summary:
👉 I would design the Rate Limiter using Token Bucket algorithm, stored in Redis,
👉 Implemented in both API Gateway and Application Layer,
👉 Using atomic Lua script,
👉 With support for per-user configs, Retry-After header,
👉 Scalable to millions of users.

# ✅Deque Rate Limit

## How It Works:
## ✅ We use a ConcurrentHashMap<String, Deque<Long>>
## ✅ For each IP, store timestamps of recent requests
## ✅ On each request:

Clean up timestamps older than 1 min

If requests in window < 5 → allow and record timestamp

If ≥ 5 → return 429

## Output:

200 OK - Request allowed for IP: 192.168.1.10

200 OK - Request allowed for IP: 192.168.1.10

200 OK - Request allowed for IP: 192.168.1.10

200 OK - Request allowed for IP: 192.168.1.10

200 OK - Request allowed for IP: 192.168.1.10

429 Too Many Requests for IP: 192.168.1.10

429 Too Many Requests for IP: 192.168.1.10

# 🚀 Why Sliding Window?
The naive approach stores all timestamps — memory usage grows if lots of IPs send many requests.

Sliding Window Counter keeps just a counter + "last reset time" per IP — it's lighter and faster.

## ✅ Key Points:
For each IP → store a Counter object:

count → how many requests in current window

windowStart → when the window started

If current time > windowStart + window size → reset counter

Memory per IP = O(1) → just 2 variables per IP

## 🚀 Advantages over the Deque-based version:
✅ Lower memory — just 1 object per IP
✅ No need to store or iterate timestamps
✅ Very fast O(1) checks
✅ Thread-safe with simple sync

# ✅ 1. Token Bucket Algorithm
How it works:

Each IP has a "bucket" with max N tokens (say 5).

Each request removes 1 token.

Tokens refill over time (e.g., 1 token per 12 seconds → 5 tokens per min).

If bucket empty → reject request.

👉 Smooth rate limiting (better for real-time APIs).

## ✅ Pros:

Smooth refill

Burst handling (if IP sends no requests for a while, tokens build up again)

Used in Stripe, AWS API Gateway

# ✅ 2. Leaky Bucket Concept:
Bucket with capacity (say, 5 "drops")

Drops leak at constant rate (ex: 1 drop per 12 sec)

Requests try to "fill" bucket:

If bucket not full → allow request → add drop

If bucket full → reject (429)

Leaks continue over time — bucket refills automatically

👉 Forces a steady flow → good for smoothing bursty traffic.

## ✅ What happens here?
Bucket size: 5

Leak rate: 1 token per 12 sec

Each request = adding 1 token

If bucket full → 429

If tokens leak out → new space opens up → allow request

# ✅ 3. Google Guava RateLimiter
If you want a production-quality ready implementation, use Guava RateLimiter:

## ✅ Pros:

Production ready

High-performance

Well-tested

You can set rate per second, minute, etc

Supports warm-up period if needed

| Algorithm         | Use case                         |
| ----------------- | -------------------------------- |
| Sliding Window    | Simple, fast, less memory        |
| Token Bucket      | Smooth refill, handles bursts    |
| Leaky Bucket      | Traffic shaping (steady outflow) |
| Guava RateLimiter | Production-ready, flexible       |


##  Here you go — a Spring Boot-ready example of using an Interceptor + in-memory RateLimiter (Sliding Window or Token Bucket) 👇

## ✅ Structure
Interceptor — runs before every request

For each IP → check if allowed → allow or return 429

No 3rd party libs needed (Guava optional)

Works with any controller

✅ Result
First 5 requests in 1 min → 200 OK

Next requests → 429 Too Many Requests

Auto resets after window

✅ Summary
Lightweight (no 3rd party required)

No annotations needed — works at middleware level

Can extend to:

Use TokenBucketRateLimiter instead

Add Redis cache to make it distributed (if running multiple servers)

Use Guava RateLimiter per IP

