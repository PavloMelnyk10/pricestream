-- Keys:
-- KEYS[1] = the unique key for the client (e.g., "rate_limit:ip:192.168.0.1")
--
-- Args:
-- ARGV[1] = max number of requests allowed in the window (e.g., 60)
-- ARGV[2] = window size in seconds (e.g., 60)
--
-- Returns:
-- 1 if allowed, 0 if rate limited

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

local current = redis.call('GET', key)

if current and tonumber(current) >= limit then
    return 0
end

current = redis.call('INCR', key)

if tonumber(current) == 1 then
    -- Set expiration on the first request in the window
    redis.call('EXPIRE', key, window)
end

return 1
