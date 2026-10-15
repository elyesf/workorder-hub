package dev.elyes.workorders.workorder;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.elyes.workorders.auth.UserRepository;

/** Full HTTP tests against the real Spring context and an in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class WorkOrderApiTest {

    private static final String EMAIL = "tester@example.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private WorkOrderRepository workOrders;

    @Autowired
    private UserRepository users;

    @BeforeEach
    void cleanDatabase() throws Exception {
        workOrders.deleteAll();
        users.deleteAll();
        register(EMAIL, "secret123");
    }

    @Test
    void registerLoginAndUseTheRealToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"TESTER@example.com\",\"password\":\"secret123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("USER"))
            .andReturn().getResponse().getContentAsString();
        String token = json.readTree(body).get("token").asText();

        mvc.perform(get("/api/work-orders").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tester@example.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsAConflict() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tester@example.com\",\"password\":\"another123\",\"displayName\":\"Again\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mvc.perform(get("/api/work-orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void createReadAndFilter() throws Exception {
        long id = create("Replace pump seal", "Stadtwerke");
        create("Inspect heating", "Kita");

        mvc.perform(get("/api/work-orders/{id}", id).with(user()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.createdBy").value("Tester"))
            .andExpect(jsonPath("$.allowedNextStatuses", containsInAnyOrder("IN_PROGRESS", "CANCELLED")));

        mvc.perform(get("/api/work-orders").param("q", "PUMP").with(user()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].title").value("Replace pump seal"));
    }

    @Test
    void invalidInputReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/work-orders").with(user())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"customer\":\"Kita\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").exists())
            .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test
    void statusRulesAreEnforced() throws Exception {
        long id = create("Replace pump seal", "Stadtwerke");

        changeStatus(id, "DONE").andExpect(status().isConflict());
        changeStatus(id, "IN_PROGRESS").andExpect(status().isOk());
        changeStatus(id, "DONE").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(get("/api/work-orders").param("status", "DONE").with(user()))
            .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void updateChangesFields() throws Exception {
        long id = create("Replace pump seal", "Stadtwerke");

        mvc.perform(put("/api/work-orders/{id}", id).with(user())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Replace pump seal and filter","customer":"Stadtwerke","priority":"HIGH","assignee":"Jonas"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Replace pump seal and filter"))
            .andExpect(jsonPath("$.assignee").value("Jonas"));
    }

    @Test
    void onlyAdminsCanDelete() throws Exception {
        long id = create("Replace pump seal", "Stadtwerke");

        mvc.perform(delete("/api/work-orders/{id}", id).with(user())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/work-orders/{id}", id).with(admin())).andExpect(status().isNoContent());
        mvc.perform(get("/api/work-orders/{id}", id).with(user())).andExpect(status().isNotFound());
    }

    @Test
    void statsCountByStatus() throws Exception {
        create("A", "Customer");
        long id = create("B", "Customer");
        changeStatus(id, "CANCELLED");

        mvc.perform(get("/api/work-orders/stats").with(user()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.byStatus.OPEN").value(1))
            .andExpect(jsonPath("$.byStatus.CANCELLED").value(1));
    }

    // helpers

    private void register(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"displayName\":\"Tester\"}"))
            .andExpect(status().isCreated());
    }

    private long create(String title, String customer) throws Exception {
        String body = mvc.perform(post("/api/work-orders").with(user())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + title + "\",\"customer\":\"" + customer + "\",\"priority\":\"MEDIUM\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        return node.get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions changeStatus(long id, String status) throws Exception {
        return mvc.perform(patch("/api/work-orders/{id}/status", id).with(user())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + status + "\"}"));
    }

    private static RequestPostProcessor user() {
        return jwt().jwt(j -> j.subject(EMAIL)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject(EMAIL)).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
