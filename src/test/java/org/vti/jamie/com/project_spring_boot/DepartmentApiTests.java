package org.vti.jamie.com.project_spring_boot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.vti.jamie.com.project_spring_boot.repository.DepartmentRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DepartmentApiTests {
    private static final String URL = "/api/v1/departments";
    @Autowired WebApplicationContext context;
    @Autowired DepartmentRepository repository;
    MockMvc mvc;

    @BeforeEach
    void setup() { mvc = MockMvcBuilders.webAppContextSetup(context)
            .defaultRequest(get("/api/").contextPath("/api")).build(); }

    @Test
    void crudAndDuplicateNames() throws Exception {
        String name = "CRUD-" + System.nanoTime();
        var created = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"  " + name + "  \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.departmentName").value(name)).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.contains("/api/v1/departments/"));
        assertFalse(location.contains("/api/api/"));
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.data.departmentName").value(name));
        mvc.perform(get(URL).param("keyword", name).param("sortBy", "departmentName").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].departmentName").value(name));
        // Updating the same name must not conflict with the department itself.
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"" + name + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"" + name.toLowerCase() + "\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"" + name + "-new\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.departmentName").value(name + "-new"));
        mvc.perform(delete(location)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mvc.perform(get(location)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        String renamed = name + "-new";
        mvc.perform(get(URL).param("keyword", renamed))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get(URL).param("keyword", renamed).param("deleted", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].deletedAt").isNotEmpty());
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"" + renamed + "\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete(location)).andExpect(status().isNotFound());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"departmentName\":\"" + renamed + "\"}"))
                .andExpect(status().isConflict());
        // Restore preserves the original row and ID.
        mvc.perform(patch(location + "/restore")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departmentName").value(renamed))
                .andExpect(jsonPath("$.data.deletedAt").isEmpty());
        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(get(URL).param("keyword", renamed).param("deleted", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(patch(location + "/restore")).andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"?page=-1", "?size=0", "?size=101", "?page=abc", "?sortBy=bad", "?direction=bad", "/0", "/256", "/abc"})
    void invalidParametersReturnEnvelope(String suffix) throws Exception {
        mvc.perform(get(URL + suffix)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void invalidBodyAndHttpMethod() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"departmentName\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.departmentName").exists());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(patch(URL)).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405)).andExpect(header().exists("Allow"));
    }

    @Test
    void duplicateApiPrefixReturnsEndpointNotFound() throws Exception {
        mvc.perform(get("/api/api/v1/departments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HTTP_404"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy endpoint. Vui lòng kiểm tra URL"));
    }

    @Test
    void cannotDeleteDepartmentWithAccounts() throws Exception {
        // Use existing seeded data; the test transaction never removes it.
        var linked = repository.findAll().stream().filter(d -> !d.getAccounts().isEmpty()).findFirst().orElseThrow();
        mvc.perform(delete(URL + "/" + linked.getId())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
        assertTrue(repository.existsById(linked.getId()));
    }
}
