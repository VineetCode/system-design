# How It Works:
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