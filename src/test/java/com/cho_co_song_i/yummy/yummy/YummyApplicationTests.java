package com.cho_co_song_i.yummy.yummy;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
})
public class YummyApplicationTests {

//	@Mock
//	private RestHighLevelClient elasticsearchClient;
//
//	@Test
//	void contextLoads() {
//	}



}
