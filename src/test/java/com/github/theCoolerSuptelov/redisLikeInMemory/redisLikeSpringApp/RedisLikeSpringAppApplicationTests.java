package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp;

import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.Services.CacheImplService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class RedisLikeSpringAppApplicationTests {
    @Autowired
    private final CacheImplService cacheImplService;

    @Autowired
    private final WebApplicationContext webAppContext;

    private MockMvc mockRedisLikeController;

    public RedisLikeSpringAppApplicationTests(@Autowired CacheImplService cacheImplService,
                                              @Autowired WebApplicationContext webAppContext,
                                              @Autowired MockMvc mockRedisLikeController) {
        this.cacheImplService = cacheImplService;
        this.webAppContext = webAppContext;
        this.mockRedisLikeController = mockRedisLikeController;
        setupMockWithSecurityContext();
    }

    public void setupMockWithSecurityContext() {
        mockRedisLikeController = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void putStringAsCachedValue() throws Exception {
        var postResponce = mockRedisLikeController
                .perform(
                        post("/api?key=testKey&EX=50").with(user("admin").roles("ADMIN"))
                                .content("{\"value\": [\"JustString\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andReturn()
                .getResponse();

        assertEquals(200, postResponce.getStatus());
        assertEquals("OK", postResponce.getContentAsString());
    }

	@Test
	public void putKeyValueCollectionAsCachedValue() throws Exception {
		var postMapResponce = mockRedisLikeController
				.perform(
						post("/api?key=mapKey&EX=50").with(user("admin").roles("ADMIN"))
								.content("{\"value\": [{\"ka\":1},{\"sl\":2},{\"oie\":3},{ \"slsl\":4},{\"zxc\":5},{\"asda\":6}]}")
								.contentType(MediaType.APPLICATION_JSON_VALUE)
				)
				.andReturn()
				.getResponse();

		assertEquals(200, postMapResponce.getStatus());
		assertEquals("OK", postMapResponce.getContentAsString());
	}
	@Test
	public void putListCollectionAsCachedValue() throws Exception {
		var postListResponce = mockRedisLikeController
				.perform(
						post("/api?key=listKey&EX=50").with(user("admin").roles("ADMIN"))
								.content("{\"value\": [1,2,3,4,5,6]}")
								.contentType(MediaType.APPLICATION_JSON_VALUE)
				)
				.andReturn()
				.getResponse();

		assertEquals(200, postListResponce.getStatus());
		assertEquals("OK", postListResponce.getContentAsString());
	}

	@Test
	public void addCachedValueCanOnlyAdmin() throws Exception {
		var postResponceByUser = mockRedisLikeController
				.perform(
						post("/api?key=testKeyAsUser&EX=50").with(user("user").roles("USER"))
								.content("{\"value\":[\"JustString\"]}")
								.contentType(MediaType.APPLICATION_JSON_VALUE)
				)
				.andReturn()
				.getResponse();

		assertEquals(403, postResponceByUser.getStatus());
	}

	@Test
	public void getCachedValue() throws Exception {
		putStringAsCachedValue();
		var getResponceAfterPostRequest = mockRedisLikeController
				.perform(
						get("/api?key=testKey").with(user("admin")))
				.andReturn()
				.getResponse();
		assertTrue(getResponceAfterPostRequest.getContentAsString().contains("JustString"));
	}

	@Test
	public void optionsStrictNameSearch() throws Exception {
		putStringAsCachedValue();
		var keysResponce = mockRedisLikeController
				.perform(get("/api/keys?key=testKey").with(user("admin")))
				.andReturn()
				.getResponse();
		assertEquals(200, keysResponce.getStatus());
		assertTrue(keysResponce.getContentAsString().contains("testKey"));
	}

	@Test
	public void optionsFindKeyByRegExp() throws Exception {
		putStringAsCachedValue();
		var keyPatternResponce = mockRedisLikeController
				.perform(get("/api/keys?pattern=testKey%").with(user("admin")))
				.andReturn()
				.getResponse();
		assertFalse(keyPatternResponce.getContentAsString().contains("testKey"));
	}

	@Test
	public void deleteExistedKey() throws Exception {
		putStringAsCachedValue();
		var deleteKey = mockRedisLikeController
				.perform(delete("/api?key=testKey").with(user("admin").roles("ADMIN")))
				.andReturn()
				.getResponse();
		assertEquals("1", deleteKey.getContentAsString());
	}

    @Test
    private void getNonexistedKey() throws Exception {
        var responseBeforePostKeyIsEmpty = mockRedisLikeController
                .perform(
                        get("/api?key=testKey").with(user("admin")))
                .andReturn()
                .getResponse();

        assertEquals(200, responseBeforePostKeyIsEmpty.getStatus());
        assertEquals(0, responseBeforePostKeyIsEmpty.getContentLength());
    }


}
