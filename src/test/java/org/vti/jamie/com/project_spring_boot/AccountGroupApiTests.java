package org.vti.jamie.com.project_spring_boot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.vti.jamie.com.project_spring_boot.dto.request.*;
import org.vti.jamie.com.project_spring_boot.dto.response.AccountResponse;
import org.vti.jamie.com.project_spring_boot.entity.GroupAccountId;
import org.vti.jamie.com.project_spring_boot.repository.*;
import org.vti.jamie.com.project_spring_boot.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AccountGroupApiTests {
    static final String ACCOUNTS = "/api/v1/accounts";
    static final String GROUPS = "/api/v1/groups";
    @Autowired WebApplicationContext context;
    @Autowired DepartmentRepository departments;
    @Autowired PositionRepository positions;
    @Autowired AccountRepository accounts;
    @Autowired GroupRepository groups;
    @Autowired GroupAccountRepository memberships;
    @Autowired AccountService accountService;
    @Autowired GroupService groupService;
    @Autowired GroupAccountService memberService;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;
    MockMvc mvc;
    Short departmentId;
    Short positionId;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/api/").contextPath("/api")).build();
        departmentId = departments.findAll().stream().filter(d -> d.getDeletedAt() == null).findFirst().orElseThrow().getId();
        positionId = positions.findAll().stream().filter(p -> p.getDeletedAt() == null).findFirst().orElseThrow().getId();
    }

    private AccountResponse newAccount() {
        String unique = "a" + System.nanoTime();
        return accountService.create(new AccountRequest(unique + "@example.com", unique, "Test Account", departmentId, positionId));
    }

    private String accountBody(String unique, Short dep, Short pos) {
        return "{\"email\":\"" + unique + "@example.com\",\"username\":\"" + unique +
                "\",\"fullName\":\"Test Account\",\"departmentId\":" + dep + ",\"positionId\":" + pos + "}";
    }

    @Test void completeAccountGroupMembershipLifecycle() throws Exception {
        String unique = "crud" + System.nanoTime();
        String accountBody = accountBody(unique, departmentId, positionId);
        var created = mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(accountBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.department.departmentId").value(departmentId.intValue()))
                .andExpect(jsonPath("$.data.position.positionId").value(positionId.intValue()))
                .andReturn();
        String accountUrl = created.getResponse().getHeader("Location");
        assertNotNull(accountUrl);
        short accountId = Short.parseShort(accountUrl.substring(accountUrl.lastIndexOf('/') + 1));
        mvc.perform(get(accountUrl)).andExpect(status().isOk());
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(accountBody))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        mvc.perform(put(accountUrl).contentType(MediaType.APPLICATION_JSON).content(
                "{\"fullName\":\"Updated Name\",\"departmentId\":" + departmentId + ",\"positionId\":" + positionId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.email").value(unique + "@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
        mvc.perform(get(ACCOUNTS).param("keyword", unique).param("departmentId", departmentId.toString())
                .param("positionId", positionId.toString()).param("sortBy", "fullName"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));

        String groupBody = "{\"groupName\":\"" + unique + "\",\"creatorId\":" + accountId + "}";
        var createdGroup = mvc.perform(post(GROUPS).contentType(MediaType.APPLICATION_JSON).content(groupBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.creator.accountId").value((int) accountId)).andReturn();
        String groupUrl = createdGroup.getResponse().getHeader("Location");
        assertNotNull(groupUrl);
        short groupId = Short.parseShort(groupUrl.substring(groupUrl.lastIndexOf('/') + 1));
        mvc.perform(post(GROUPS).contentType(MediaType.APPLICATION_JSON).content(groupBody)).andExpect(status().isConflict());
        mvc.perform(put(groupUrl).contentType(MediaType.APPLICATION_JSON).content(
                "{\"groupName\":\"" + unique + "-new\",\"creatorId\":" + accountId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.groupName").value(unique + "-new"));
        mvc.perform(get(GROUPS).param("keyword", unique).param("creatorId", Short.toString(accountId))
                .param("sortBy", "groupName")).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        String membersUrl = groupUrl + "/members";
        String memberBody = "{\"accountId\":" + accountId + "}";
        mvc.perform(post(membersUrl).contentType(MediaType.APPLICATION_JSON).content(memberBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.groupId").value((int) groupId))
                .andExpect(jsonPath("$.data.account.accountId").value((int) accountId))
                .andExpect(jsonPath("$.data.joinedAt").isNotEmpty());
        mvc.perform(post(membersUrl).contentType(MediaType.APPLICATION_JSON).content(memberBody))
                .andExpect(status().isConflict());
        var key = new GroupAccountId(groupId, accountId);
        em.flush(); em.clear();
        assertTrue(memberships.existsById(key));
        mvc.perform(get(membersUrl).param("keyword", unique).param("sortBy", "username"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].account.department.departmentId").value(departmentId.intValue()));
        mvc.perform(get(membersUrl + "/" + accountId)).andExpect(status().isOk());

        mvc.perform(delete(accountUrl)).andExpect(status().isOk());
        mvc.perform(get(accountUrl)).andExpect(status().isNotFound());
        mvc.perform(get(ACCOUNTS).param("keyword", unique)).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get(ACCOUNTS).param("keyword", unique).param("deleted", "true"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get(membersUrl)).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(post(membersUrl).contentType(MediaType.APPLICATION_JSON).content(memberBody))
                .andExpect(status().isConflict());
        // Soft deletion preserves creator and membership FKs; group restore validates the creator.
        mvc.perform(delete(groupUrl)).andExpect(status().isOk());
        mvc.perform(get(groupUrl)).andExpect(status().isNotFound());
        mvc.perform(get(membersUrl)).andExpect(status().isNotFound());
        mvc.perform(get(GROUPS).param("keyword", unique).param("deleted", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(patch(groupUrl + "/restore")).andExpect(status().isConflict());
        mvc.perform(patch(accountUrl + "/restore")).andExpect(status().isOk());
        mvc.perform(patch(groupUrl + "/restore")).andExpect(status().isOk());
        mvc.perform(get(membersUrl)).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(patch(accountUrl + "/restore")).andExpect(status().isConflict());
        mvc.perform(patch(groupUrl + "/restore")).andExpect(status().isConflict());
        mvc.perform(delete(membersUrl + "/" + accountId)).andExpect(status().isOk());
        em.flush(); em.clear();
        assertFalse(memberships.existsById(key));
        assertTrue(accounts.existsById(accountId));
        assertTrue(groups.existsById(groupId));
        mvc.perform(delete(membersUrl + "/" + accountId)).andExpect(status().isNotFound());
        mvc.perform(post(membersUrl).contentType(MediaType.APPLICATION_JSON).content(memberBody)).andExpect(status().isCreated());
    }

    @Test void validatesForeignKeysAndRollsBackFailedWrites() throws Exception {
        String unique = "fk" + System.nanoTime();
        long count = accounts.count();
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(accountBody(unique, (short) 255, positionId)))
                .andExpect(status().isNotFound());
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(accountBody(unique, departmentId, (short) 255)))
                .andExpect(status().isNotFound());
        assertEquals(count, accounts.count());
        var department = departments.findById(departmentId).orElseThrow();
        department.softDelete(); departments.saveAndFlush(department);
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(accountBody(unique, departmentId, positionId)))
                .andExpect(status().isConflict());
        department.restore(); departments.saveAndFlush(department);
        var account = newAccount();
        var group = groupService.create(new GroupRequest(unique, account.accountId()));
        String groupUrl = GROUPS + "/" + group.groupId();
        mvc.perform(post(groupUrl + "/members").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":255}"))
                .andExpect(status().isNotFound());
        accountService.delete(account.accountId());
        long groupCount = groups.count();
        mvc.perform(post(GROUPS).contentType(MediaType.APPLICATION_JSON).content(
                "{\"groupName\":\"another\",\"creatorId\":" + account.accountId() + "}"))
                .andExpect(status().isConflict());
        assertEquals(groupCount, groups.count());
        mvc.perform(post(GROUPS).contentType(MediaType.APPLICATION_JSON).content("{\"groupName\":\"another\",\"creatorId\":255}"))
                .andExpect(status().isNotFound());
        var position = positions.findById(positionId).orElseThrow();
        position.softDelete(); positions.saveAndFlush(position);
        mvc.perform(patch(ACCOUNTS + "/" + account.accountId() + "/restore")).andExpect(status().isConflict());
        assertNotNull(accounts.findById(account.accountId()).orElseThrow().getDeletedAt());
    }

    @Test void emailAndUsernameStayUniqueAcrossDeletedAccounts() throws Exception {
        var account = newAccount();
        accountService.delete(account.accountId());
        String unique = "other" + System.nanoTime();
        String body = accountBody(unique, departmentId, positionId).replace(unique + "@example.com", account.email());
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        body = accountBody(unique, departmentId, positionId).replace("\"username\":\"" + unique + "\"",
                "\"username\":\"" + account.username() + "\"");
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }

    @Test void removalAllowsDeletedAccountAndDoesNotCascadeToParents() throws Exception {
        var creator = newAccount();
        var member = newAccount();
        var group = groupService.create(new GroupRequest("remove" + System.nanoTime(), creator.accountId()));
        memberService.add(group.groupId(), member.accountId());
        accountService.delete(member.accountId());
        mvc.perform(delete(GROUPS + "/" + group.groupId() + "/members/" + member.accountId())).andExpect(status().isOk());
        em.flush(); em.clear();
        assertTrue(groups.existsById(group.groupId()));
        assertTrue(accounts.existsById(member.accountId()));
        assertFalse(memberships.existsById(new GroupAccountId(group.groupId(), member.accountId())));
    }

    @Test void compositeKeyIsolatesEachGroupAndAccountPair() {
        var first = newAccount();
        var second = newAccount();
        var firstGroup = groupService.create(new GroupRequest("pairA" + System.nanoTime(), first.accountId()));
        var secondGroup = groupService.create(new GroupRequest("pairB" + System.nanoTime(), first.accountId()));
        memberService.add(firstGroup.groupId(), first.accountId());
        memberService.add(firstGroup.groupId(), second.accountId());
        memberService.add(secondGroup.groupId(), first.accountId());
        memberService.remove(firstGroup.groupId(), first.accountId());
        em.flush(); em.clear();
        assertFalse(memberships.existsById(new GroupAccountId(firstGroup.groupId(), first.accountId())));
        assertTrue(memberships.existsById(new GroupAccountId(firstGroup.groupId(), second.accountId())));
        assertTrue(memberships.existsById(new GroupAccountId(secondGroup.groupId(), first.accountId())));
    }

    @Test void accountUpdateChangesForeignKeysWithoutChangingIdentity() throws Exception {
        var account = newAccount();
        var department = departments.saveAndFlush(new org.vti.jamie.com.project_spring_boot.entity.Department("move" + System.nanoTime()));
        var position = positions.findAll().stream().filter(p -> p.getDeletedAt() == null && !p.getId().equals(positionId))
                .findFirst().orElseThrow();
        mvc.perform(put(ACCOUNTS + "/" + account.accountId()).contentType(MediaType.APPLICATION_JSON).content(
                "{\"fullName\":\"Moved User\",\"departmentId\":" + department.getId() + ",\"positionId\":" + position.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.departmentId").value(department.getId().intValue()))
                .andExpect(jsonPath("$.data.position.positionId").value(position.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value(account.username()));
        em.flush(); em.clear();
        var persisted = accounts.findById(account.accountId()).orElseThrow();
        assertEquals(department.getId(), persisted.getDepartment().getId());
        assertEquals(position.getId(), persisted.getPosition().getId());
    }

    @Test void entityGraphsAvoidNPlusOneAndPreserveDatabasePaging() {
        var stats = emf.unwrap(SessionFactory.class).getStatistics();
        boolean enabledBefore = stats.isStatisticsEnabled();
        stats.setStatisticsEnabled(true);
        try {
            em.clear(); stats.clear();
            var accountPage = accountService.getAll(null, false, null, null, PageRequest.of(0, 5));
            assertEquals(5, accountPage.getContent().size());
            assertTrue(stats.getPrepareStatementCount() <= 2, "Account list should use page + count only");
            assertEquals(0, stats.getEntityFetchCount());
            assertEquals(0, stats.getCollectionFetchCount());
            em.clear(); stats.clear();
            var groupPage = groupService.getAll(null, false, null, PageRequest.of(0, 5));
            assertEquals(5, groupPage.getContent().size());
            assertTrue(stats.getPrepareStatementCount() <= 2, "Group list should use page + count only");
            assertEquals(0, stats.getEntityFetchCount());
            assertEquals(0, stats.getCollectionFetchCount());
            em.clear(); stats.clear();
            var memberPage = memberService.getAll((short) 1, null, PageRequest.of(0, 2));
            assertEquals(2, memberPage.getContent().size());
            assertTrue(stats.getPrepareStatementCount() <= 3, "Membership list should use parent lookup + page + count only");
            assertEquals(0, stats.getEntityFetchCount());
            assertEquals(0, stats.getCollectionFetchCount());
        } finally { stats.setStatisticsEnabled(enabledBefore); }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/0", "/256", "/abc", "?page=-1", "?size=0", "?size=101", "?sortBy=bad", "?direction=bad", "?departmentId=0"})
    void validatesAccountParameters(String suffix) throws Exception {
        mvc.perform(get(ACCOUNTS + suffix)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/0", "/256", "/abc", "?page=-1", "?size=101", "?sortBy=bad", "?direction=bad", "?creatorId=0"})
    void validatesGroupParameters(String suffix) throws Exception {
        mvc.perform(get(GROUPS + suffix)).andExpect(status().isBadRequest());
    }

    @Test void validatesRequestBodiesAndMissingParents() throws Exception {
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists());
        String body = accountBody("bad", departmentId, positionId).replace("bad@example.com", "bad-email");
        mvc.perform(post(ACCOUNTS).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        mvc.perform(post(GROUPS).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.creatorId").exists());
        mvc.perform(post(GROUPS + "/1/members").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.accountId").exists());
        mvc.perform(get(GROUPS + "/255/members")).andExpect(status().isNotFound());
        mvc.perform(patch(ACCOUNTS + "/255/restore")).andExpect(status().isNotFound());
        mvc.perform(patch(GROUPS + "/255/restore")).andExpect(status().isNotFound());
    }
}
