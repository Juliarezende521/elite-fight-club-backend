package Tic.Implementacao.controller;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProfessorAuthenticationIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter security;

    @Test
    void semTokenRetorna401NaCadeiaReal() throws Exception {
        MockMvcBuilders.webAppContextSetup(context).addFilters(security).build()
                .perform(get("/professores").servletPath("/professores"))
                .andExpect(status().isUnauthorized());
    }
}
