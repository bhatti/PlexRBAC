package com.plexobject.rbac.security;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.domain.Permission;
import com.plexobject.rbac.domain.Role;
import com.plexobject.rbac.domain.Subject;
import com.plexobject.rbac.eval.js.JavascriptEvaluator;
import com.plexobject.rbac.repository.PermissionRepository;
import com.plexobject.rbac.repository.RepositoryFactory;
import com.plexobject.rbac.repository.RoleRepository;
import com.plexobject.rbac.repository.SecurityMappingRepository;
import com.plexobject.rbac.repository.SubjectRepository;
import com.plexobject.rbac.repository.bdb.RepositoryFactoryImpl;
import com.plexobject.rbac.utils.CurrentRequest;
import com.plexobject.rbac.utils.IDUtils;

public class PermissionManagerTest {
    private static final String USER_NAME = "shahbhat";
    private static final String APP_NAME = "pappname";
    private static final String BANKING = "banking";

    private static final String TEST_DB_DIR = "test_db_dir_perms";
    private RepositoryFactory repositoryFactory;
    private PermissionManager permissionManager;

    static class User {
        private String id;
        private String region;

        User() {
        }

        public User(String id, String region) {
            this.id = id;
            this.region = region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRegion() {
            return region;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    static class Customer extends User {
        public Customer(String id, String region) {
            super(id, region);
        }
    }

    static class Employee extends User {
        public Employee(String id, String region) {
            super(id, region);
        }
    }

    static class Account {
        private String id;
        private double balance;

        Account() {
        }

        public Account(String id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }
    }

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
        if (repositoryFactory != null) {
            ((RepositoryFactoryImpl) repositoryFactory).close(APP_NAME);
        }
        FileUtils.deleteDirectory(new File(TEST_DB_DIR));
        CurrentRequest.endRequest();
    }

    @Test
    public void testCheck() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "read", "database", IDUtils.toMap("amount", "100", "dept",
                        "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadOperation() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "xread", "database", IDUtils.toMap("amount", "100", "dept",
                        "sales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadDept() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "write", "database", IDUtils.toMap("amount", "100", "dept",
                        "xsales")));
    }

    @Test(expected = SecurityException.class)
    public void testCheckBadAmount() {
        permissionManager.check(new PermissionRequest(APP_NAME, USER_NAME,
                "write", "database", IDUtils.toMap("amount", "1000", "dept",
                        "sales")));
    }

    @Test
    public void testReadDepositByTeller() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "tom", "read",
                "DepositAccount", IDUtils.toMap("employee", new Employee("tom",
                        "west"), "customer", new Customer("zak", "west"))));
    }

    @Test(expected = SecurityException.class)
    public void testDeleteByTeller() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "tom", "delete",
                "DepositAccount", IDUtils.toMap("employee", new Employee("tom",
                        "west"), "customer", new Customer("zak", "west"))));
    }

    @Test
    public void testDeleteByCsr() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "cassy",
                "delete", "DepositAccount", IDUtils.toMap("employee",
                        new Employee("cassy", "west"), "customer",
                        new Customer("zak", "west"))));
    }

    @Test
    public void testReadLedgerByAccountant() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "ali", "read",
                "GeneralLedger", IDUtils.toMap("year", 2009, "account",
                        new Account("zak", 500))));
    }

    @Test
    public void testReadLedgerByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike", "read",
                "GeneralLedger", IDUtils.toMap("year", 2009, "account",
                        new Account("zak", 500))));
    }

    @Test(expected = SecurityException.class)
    public void testDeleteLedgerByAccountant() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "ali", "delete",
                "GeneralLedger", IDUtils.toMap("year", 2009, "account",
                        new Account("zak", 500))));
    }

    @Test
    public void testCreateLedgerByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike",
                "create", "GeneralLedger", IDUtils.toMap("year", 2009,
                        "account", new Account("zak", 500))));
    }

    @Test(expected = SecurityException.class)
    public void testPostLedgingRulesByAccountantManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "mike",
                "create", "GeneralLedgerPostingRules", IDUtils.toMap("year",
                        2009, "account", new Account("zak", 500))));
    }

    @Test
    public void testPostLedgingRulesByLoanManager() {
        initDatabase();
        permissionManager.check(new PermissionRequest(BANKING, "larry",
                "create", "GeneralLedgerPostingRules", IDUtils.toMap("year",
                        2009, "account", new Account("zak", 500))));
    }

    private void initDatabase() {
        // creating domain
        repositoryFactory.getDomainRepository().save(new Domain(BANKING, ""));

        // creating users
        final SubjectRepository subjectRepo = repositoryFactory
                .getSubjectRepository(BANKING);
        Subject tom = subjectRepo.save(new Subject("tom", "pass"));
        Subject cassy = subjectRepo.save(new Subject("cassy", "pass"));
        Subject ali = subjectRepo.save(new Subject("ali", "pass"));
        Subject mike = subjectRepo.save(new Subject("mike", "pass"));
        Subject larry = subjectRepo.save(new Subject("larry", "pass"));

        //
        final RoleRepository roleRepo = repositoryFactory
                .getRoleRepository(BANKING);
        Role employee = roleRepo.save(new Role("Employee"));
        Role teller = roleRepo.save(new Role("Teller", employee));
        Role csr = roleRepo.save(new Role("CSR", teller));
        Role accountant = roleRepo.save(new Role("Accountant", employee));
        Role accountantMgr = roleRepo.save(new Role("AccountingManager",
                accountant));
        Role loanOfficer = roleRepo
                .save(new Role("LoanOfficer", accountantMgr));

        // creating permissions
        final PermissionRepository permRepo = repositoryFactory
                .getPermissionRepository(BANKING);
        Permission cdDeposit = permRepo.save(new Permission("(create|delete)",
                "DepositAccount",
                "employee.getRegion().equals(customer.getRegion())")); // 1
        Permission ruDeposit = permRepo.save(new Permission("(read|modify)",
                "DepositAccount",
                "employee.getRegion().equals(customer.getRegion())")); // 2
        Permission cdLoan = permRepo.save(new Permission("(create|delete)",
                "LoanAccount", "account.getBalance() < 10000")); // 3
        Permission ruLoan = permRepo.save(new Permission("(read|modify)",
                "LoanAccount", "account.getBalance() < 10000")); // 4

        Permission rdLedger = permRepo.save(new Permission("(read|create)",
                "GeneralLedger", "year <= new Date().getFullYear()")); // 5

        Permission rGlpr = permRepo
                .save(new Permission("read", "GeneralLedgerPostingRules",
                        "year <= new Date().getFullYear()")); // 6

        Permission cmdGlpr = permRepo.save(new Permission(
                "(create|modify|delete)", "GeneralLedgerPostingRules",
                "year <= new Date().getFullYear()")); // 7

        // Mapping Permissions to Roles
        final SecurityMappingRepository smr = repositoryFactory
                .getSecurityMappingRepository(BANKING);
        smr.addPermissionsToRole(teller, ruDeposit);
        smr.addPermissionsToRole(csr, cdDeposit);
        smr.addPermissionsToRole(accountant, rdLedger);
        smr.addPermissionsToRole(accountant, ruLoan);
        smr.addPermissionsToRole(accountantMgr, cdLoan);

        smr.addPermissionsToRole(accountantMgr, rGlpr);

        smr.addPermissionsToRole(loanOfficer, cmdGlpr);

        // Mapping Users to Roles
        smr.addRolesToSubject(tom, teller);
        smr.addRolesToSubject(cassy, csr);
        smr.addRolesToSubject(ali, accountant);
        smr.addRolesToSubject(mike, accountantMgr);
        smr.addRolesToSubject(larry, loanOfficer);
    }

    private void addPermissions() {
        //
        final SubjectRepository subjectRepo = repositoryFactory
                .getSubjectRepository(APP_NAME);
        Subject shahbhat = new Subject(USER_NAME, "credentials");
        subjectRepo.save(shahbhat);

        Subject bhatsha = new Subject("bhatsha", "credentials");
        subjectRepo.save(bhatsha);

        repositoryFactory.getDomainRepository().save(new Domain(APP_NAME, ""));

        //
        final RoleRepository roleRepo = repositoryFactory
                .getRoleRepository(APP_NAME);
        Role normal = roleRepo.save(new Role("normal"));
        Role admin = roleRepo.save(new Role("admin", normal));

        //
        final PermissionRepository permRepo = repositoryFactory
                .getPermissionRepository(APP_NAME);
        Permission read = permRepo
                .save(new Permission("read", "database", null));

        Permission wild = permRepo.save(new Permission("*", "*", ""));

        Permission crud = permRepo.save(new Permission(
                "(read|write|update|delete)", "database",
                "amount <= 500 && dept == 'sales'"));

        Permission print = permRepo.save(new Permission("print", "database",
                "company == 'plexobjects'"));

        //
        final SecurityMappingRepository smr = repositoryFactory
                .getSecurityMappingRepository(APP_NAME);
        smr.addRolesToSubject(shahbhat, normal);
        smr.addRolesToSubject(bhatsha, admin);
        smr.addPermissionsToRole(Role.ANONYMOUS, read);
        smr.addPermissionsToRole(normal, print, crud);
        smr.addPermissionsToRole(admin, wild);
    }
}
