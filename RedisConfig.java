package com.emcoo.rest.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * @Auther: boyi.liu
 * @Date: 2019/9/9 09:35
 * @Description: Redis配置类
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

	@Autowired
	private JedisConnectionFactory jedisConnectionFactory;

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		// 设置自动key的生成规则，配置spring boot的注解，进行方法级别的缓存
		// 使用:进行分割，可以很多显示出层级关系
		return (target, method, params) -> {
			StringBuilder sb = new StringBuilder();
			sb.append(target.getClass().getName());
			sb.append(":");
			sb.append(method.getName());
			for (Object obj : params) {
				sb.append(":" + String.valueOf(obj));
			}
			String rsToUse = String.valueOf(sb);
			log.info("自动生成Redis Key -> [{}]", rsToUse);
			return rsToUse;
		};
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		// 初始化缓存管理器，在这里我们可以缓存的整体过期时间什么的，这里默认没有配置
		log.info("初始化 -> [{}]", "CacheManager RedisCacheManager Start");
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
				.RedisCacheManagerBuilder
				.fromConnectionFactory(jedisConnectionFactory);
		return builder.build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory ) {
		//设置序列化
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		// 配置redisTemplate
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory);
		RedisSerializer stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer); // key序列化
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer); // value序列化
		redisTemplate.setHashKeySerializer(stringSerializer); // Hash key序列化
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer); // Hash value序列化
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Override
	@Bean
	public CacheErrorHandler errorHandler() {
		// 异常处理，当Redis发生异常时，打印日志，但是程序正常走
		log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
		CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
				log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
			}

			@Override
			public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
				log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
			}

			@Override
			public void handleCacheEvictError(RuntimeException e, Cache cache, Object key)    {
				log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
			}

			@Override
			public void handleCacheClearError(RuntimeException e, Cache cache) {
				log.error("Redis occur handleCacheClearError：", e);
			}
		};
		return cacheErrorHandler;
	}

	/**
	 * 读取配置数据，创建JedisConnectionFactory和JedisPool，以供外部类初始化缓存管理器使用
	 */
	@ConfigurationProperties
	class DataJedisProperties{
		@Value("${spring.redis.host}")
		private  String host;
		@Value("${spring.redis.password}")
		private  String password;
		@Value("${spring.redis.port}")
		private  int port;
		@Value("${spring.redis.timeout}")
		private  int timeout;
		@Value("${spring.redis.database}")
		private int database;
		@Value("${spring.redis.jedis.pool.max-idle}")
		private int maxIdle;
		@Value("${spring.redis.jedis.pool.max-wait}")
		private long maxWaitMillis;

		@Bean
		JedisConnectionFactory jedisConnectionFactory() {
			log.info("Create JedisConnectionFactory successful");

			RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration ();
			redisStandaloneConfiguration.setHostName(host);
			redisStandaloneConfiguration.setPort(port);
			redisStandaloneConfiguration.setDatabase(database);
			redisStandaloneConfiguration.setPassword(RedisPassword.of(password));

			JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
			// connection timeout
			jedisClientConfiguration.connectTimeout(Duration.ofMillis(timeout));

			JedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration,
					jedisClientConfiguration.build());
			return factory;
		}
		@Bean
		public JedisPool redisPoolFactory() {
			log.info("JedisPool init successful，host -> [{}]；port -> [{}]", host, port);
			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
			jedisPoolConfig.setMaxIdle(maxIdle);
			jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);

			JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
			return jedisPool;
		}
	}

}