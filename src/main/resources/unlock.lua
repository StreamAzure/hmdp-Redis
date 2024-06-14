if (redis.call('GET', KEYS[1]) == ARGV[1])
	return redis.call('DEL', KEYS[1])
end
return 0