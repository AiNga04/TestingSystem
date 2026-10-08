package org.vti.jamie.com.project_spring_boot;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.vti.jamie.com.project_spring_boot.repository.PositionRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class PositionApiTests {
    static final String URL = "/api/v1/positions";
    @Autowired WebApplicationContext context;
    @Autowired PositionRepository repository;
    MockMvc mvc;
    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/api/").contextPath("/api")).build();
    }

    @Test
    void readsUpdatesDuplicatesAndRestores() throws Exception {
        var position = repository.findAll().stream().filter(p -> p.getDeletedAt() == null).findFirst().orElseThrow();
        String location = URL + "/" + position.getId();
        String body = "{\"positionName\":\"" + position.getName() + "\"}";
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.positionName").value(position.getName().name()));
        mvc.perform(get(URL).param("keyword", "Dev").param("sortBy", "positionName"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        // Prepare a deleted row in the rollback transaction; existing accounts remain intact.
        position.softDelete();
        repository.saveAndFlush(position);
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isNotFound());
        mvc.perform(delete(location)).andExpect(status().isNotFound());
        mvc.perform(get(URL).param("deleted", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].deletedAt").isNotEmpty());
        mvc.perform(patch(location + "/restore")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.positionId").value(position.getId().intValue()))
                .andExpect(jsonPath("$.data.deletedAt").isEmpty());
        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(patch(location + "/restore")).andExpect(status().isConflict());
    }

    @Test
    void protectsPositionWithAccounts() throws Exception {
        var linked = repository.findAll().stream().filter(p -> !p.getAccounts().isEmpty()).findFirst().orElseThrow();
        mvc.perform(delete(URL + "/" + linked.getId())).andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"positionName\":null}", "{\"positionName\":\"\"}", "{\"positionName\":\"MANAGER\"}", "{"})
    void validatesName(String body) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/0", "/256", "/abc", "?page=-1", "?size=101", "?sortBy=bad", "?direction=bad", "?deleted=bad"})
    void validatesParameters(String suffix) throws Exception {
        mvc.perform(get(URL + suffix)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
