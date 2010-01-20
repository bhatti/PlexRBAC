package com.plexobject.rbac.security;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.bdb.RepositoryFactoryImpl;
import com.plexobject.rbac.utils.CurrentRequest;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "pappname";
    private static final String BANKING = "banking";

    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private RepositoryFactory repositoryFactory;
    private PermissionManager permissionManager;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));

        CurrentRequest.startRequest(APP_NAME, USER_NAME, "127.0.0.1");
        repositoryFactory = new RepositoryFactoryImpl(TEST_DB_DIR);

        permissionManager = new PermissionManagerImpl(repositoryFactory,
                new JavascriptEvaluator());
        addPermissions();

    }

    @After
    public void tearDown() throws Exception {
        ((RepositoryFactoryImpl) repositoryFactory).close(APP_NAME);
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentRequest.endRequest();
    }

    @Test
    public void testCheck() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "read", "database", toMap("amount", "100", "dept", "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "xread", "database", toMap("amount", "100", "dept", "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadDept() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "write", "database", toMap("amount", "100", "dept", "xsales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadAmount() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "write", "database", toMap("amount", "1000", "dept", "sales")));
    }

    @Test
    public void testReadDepositByTeller() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "tom", "read",
                "DepositAccount", toMap()));
    }

    @Test(expected = SecurityException.class)
    public void testDeleteByTeller() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "tom", "delete",
                "DepositAccount", toMap()));
    }

    @Test
    public void testDeleteByCsr() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "cassy",
                "delete", "DepositAccount", toMap()));
    }

    @Test
    public void testReadLedgerByAccountant() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "ali", "read",
                "GeneralLedger", toMap()));
    }

    @Test
    public void testReadLedgerByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike", "read",
                "GeneralLedger", toMap()));
    }

    @Test(expected = SecurityException.class)
    public void testCreateLedgerByAccountant() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "ali", "create",
                "GeneralLedger", toMap()));
    }

    @Test
    public void testCreateLedgerByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike",
                "create", "GeneralLedger", toMap()));
    }

    @Test(expected = SecurityException.class)
    public void testPostLedgingRulesByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike",
                "create", "GeneralLedgerPostingRules", toMap()));
    }

    @Test
    public void testPostLedgingRulesByLoanManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "larry",
                "create", "GeneralLedgerPostingRules", toMap()));
    }

    private static Map<String, String> toMap(final String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void initDatabase() {
        // creating domain
        repositoryFactory.getDomainRepository().save(new Domain(BANKING, ""));

        // creating users
        Subject tom = repositoryFactory.getSubjectRepository(BANKING).save(
                new Subject("tom", "pass"));
        Subject cassy = repositoryFactory.getSubjectRepository(BANKING).save(
                new Subject("cassy", "pass"));
        Subject ali = repositoryFactory.getSubjectRepository(BANKING).save(
                new Subject("ali", "pass"));
        Subject mike = repositoryFactory.getSubjectRepository(BANKING).save(
                new Subject("mike", "pass"));
        Subject larry = repositoryFactory.getSubjectRepository(BANKING).save(
                new Subject("larry", "pass"));

        Role employee = new Role("Employee");
        repositoryFactory.getRoleRepository(BANKING).save(employee);

        Role teller = new Role("Teller", employee);
        repositoryFactory.getRoleRepository(BANKING).save(teller);
        Role csr = new Role("CSR", teller);
        repositoryFactory.getRoleRepository(BANKING).save(csr);
        Role accountant = new Role("Accountant", employee);
        repositoryFactory.getRoleRepository(BANKING).save(accountant);
        Role accountantMgr = new Role("AccountingManager", accountant);
        repositoryFactory.getRoleRepository(BANKING).save(accountantMgr);
        Role loanOfficer = new Role("LoanOfficer", accountantMgr);
        repositoryFactory.getRoleRepository(BANKING).save(loanOfficer);

        // creating permissions
        Permission cdDeposit = new Permission("(create|delete)",
                "DepositAccount", ""); // 1
        repositoryFactory.getPermissionRepository(BANKING).save(cdDeposit);
        Permission ruDeposit = new Permission("(read|modify)",
                "DepositAccount", ""); // 2
        repositoryFactory.getPermissionRepository(BANKING).save(ruDeposit);

        Permission cdLoan = new Permission("(create|delete)", "LoanAccount", ""); // 3
        repositoryFactory.getPermissionRepository(BANKING).save(cdLoan);
        Permission ruLoan = new Permission("(read|modify)", "LoanAccount", ""); // 4
        repositoryFactory.getPermissionRepository(BANKING).save(ruLoan);

        Permission rdLedger = new Permission("(read|create)", "GeneralLedger",
                ""); // 5
        repositoryFactory.getPermissionRepository(BANKING).save(rdLedger);

        Permission glpr = new Permission("(read|create|modify|delete)",
                "GeneralLedgerPostingRules", ""); // 6
        repositoryFactory.getPermissionRepository(BANKING).save(glpr);

        // Mapping Permissions to Roles
        repositoryFactory.getSecurityRepository().addPermissionsToRole(BANKING,
                teller.getId(), Arrays.asList(ruDeposit.getId()));
        repositoryFactory.getSecurityRepository().addPermissionsToRole(BANKING,
                csr.getId(), Arrays.asList(cdDeposit.getId()));
        repositoryFactory.getSecurityRepository().addPermissionsToRole(BANKING,
                csr.getId(), Arrays.asList(cdDeposit.getId()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(BANKING,
                accountantMgr.getId(), Arrays.asList(glpr.getId()));

        // Mapping Users to Roles
        repositoryFactory.getSecurityRepository().addRolesToSubject(BANKING,
                tom.getId(), Arrays.asList(teller.getId()));
        repositoryFactory.getSecurityRepository().addRolesToSubject(BANKING,
                cassy.getId(), Arrays.asList(csr.getId()));
        repositoryFactory.getSecurityRepository().addRolesToSubject(BANKING,
                ali.getId(), Arrays.asList(accountant.getId()));
        repositoryFactory.getSecurityRepository().addRolesToSubject(BANKING,
                mike.getId(), Arrays.asList(accountantMgr.getId()));
        repositoryFactory.getSecurityRepository().addRolesToSubject(BANKING,
                larry.getId(), Arrays.asList(loanOfficer.getId()));
    }

    private void addPermissions() {

        //
        Subject shahbhat = new Subject(USER_NAME, "credentials");
        repositoryFactory.getSubjectRepository(APP_NAME).save(shahbhat);

        Subject bhatsha = new Subject("bhatsha", "credentials");
        repositoryFactory.getSubjectRepository(APP_NAME).save(bhatsha);

        repositoryFactory.getDomainRepository().save(new Domain(APP_NAME, ""));

        //
        Role normal = new Role("normal");
        repositoryFactory.getRoleRepository(APP_NAME).save(normal);

        Role admin = new Role("admin", normal);
        repositoryFactory.getRoleRepository(APP_NAME).save(admin);

        Permission read = new Permission("read", "database", null);
        repositoryFactory.getPermissionRepository(APP_NAME).save(read);

        Permission wild = new Permission("*", "*", "");
        repositoryFactory.getPermissionRepository(APP_NAME).save(wild);

        Permission crud = new Permission("(read|write|update|delete)",
                "database", "amount <= 500 && dept == 'sales'");
        repositoryFactory.getPermissionRepository(APP_NAME).save(crud);

        Permission print = new Permission("print", "database",
                "company == 'plexobjects'");
        repositoryFactory.getPermissionRepository(APP_NAME).save(print);

        //
        repositoryFactory.getSecurityRepository().addRolesToSubject(APP_NAME,
                shahbhat.getId(), Arrays.asList("normal"));

        repositoryFactory.getSecurityRepository().addRolesToSubject(APP_NAME,
                bhatsha.getId(), Arrays.asList("admin"));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, Role.ANONYMOUS.getId(), Arrays.asList(read.getId()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, normal.getId(),
                Arrays.asList(print.getId(), crud.getId()));

        repositoryFactory.getSecurityRepository().addPermissionsToRole(
                APP_NAME, admin.getId(), Arrays.asList(wild.getId()));
    }
}
