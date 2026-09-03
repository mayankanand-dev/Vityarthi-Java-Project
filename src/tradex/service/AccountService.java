package tradex.service;

import tradex.exception.AuthenticationException;
import tradex.exception.TradeXException;
import tradex.model.Account;
import tradex.model.Transaction;
import tradex.model.User;
import tradex.repository.AccountRepository;
import tradex.repository.TransactionRepository;
import tradex.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;

    public AccountService(UserRepository userRepo, AccountRepository accountRepo, TransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(password.hashCode());
        }
    }

    public User register(String username, String password, String role, double initialCash) throws TradeXException {
        if (username == null || username.trim().length() < 3) {
            throw new TradeXException("Username must be at least 3 characters long.");
        }
        if (password == null || password.trim().length() < 4) {
            throw new TradeXException("Password must be at least 4 characters long.");
        }
        if (userRepo.findByUsername(username).isPresent()) {
            throw new TradeXException("User '" + username + "' already exists.");
        }

        try {
            User newUser = new User(username.trim(), hashPassword(password), role);
            User savedUser = userRepo.save(newUser);

            double depositAmount = Math.max(1000.0, initialCash);
            Account account = new Account(savedUser.getId(), depositAmount);
            Account savedAccount = accountRepo.save(account);

            txRepo.save(new Transaction(savedAccount.getAccountId(), "INITIAL_DEPOSIT", depositAmount, depositAmount, "Initial virtual account grant"));

            return savedUser;
        } catch (SQLException e) {
            throw new TradeXException("Database error during registration: " + e.getMessage(), e);
        }
    }

    public User login(String username, String password) throws TradeXException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        if (!user.getPasswordHash().equals(hashPassword(password))) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return user;
    }

    public Optional<Account> getAccountByUserId(int userId) {
        return accountRepo.findByUserId(userId);
    }

    public Optional<Account> getAccount(int accountId) {
        return accountRepo.findById(accountId);
    }

    public synchronized void deposit(int accountId, double amount) throws TradeXException {
        if (amount <= 0) {
            throw new TradeXException("Deposit amount must be positive.");
        }
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new TradeXException("Account not found."));

        account.deposit(amount);
        try {
            accountRepo.updateBalances(account);
            txRepo.save(new Transaction(accountId, "DEPOSIT", amount, account.getCashBalance(), "Virtual cash deposit"));
        } catch (SQLException e) {
            throw new TradeXException("Failed to record deposit: " + e.getMessage());
        }
    }

    public synchronized void withdraw(int accountId, double amount) throws TradeXException {
        if (amount <= 0) {
            throw new TradeXException("Withdrawal amount must be positive.");
        }
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new TradeXException("Account not found."));

        if (account.getAvailableCash() < amount) {
            throw new TradeXException(String.format("Insufficient available balance. Available: ₹%.2f, Requested: ₹%.2f",
                    account.getAvailableCash(), amount));
        }

        account.withdraw(amount);
        try {
            accountRepo.updateBalances(account);
            txRepo.save(new Transaction(accountId, "WITHDRAWAL", -amount, account.getCashBalance(), "Virtual cash withdrawal"));
        } catch (SQLException e) {
            throw new TradeXException("Failed to record withdrawal: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactions(int accountId) {
        return txRepo.listByAccountId(accountId);
    }
}
