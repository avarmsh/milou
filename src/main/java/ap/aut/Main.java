package ap.aut;

import ap.aut.dao.UserDao;
import ap.aut.model.Email;
import ap.aut.model.User;
import ap.aut.service.MilouService;
import ap.aut.util.HibernateUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final MilouService svc = new MilouService(new UserDao(), new ap.aut.dao.EmailDao());
    private static final Set<String> starredCodes = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Welcome to Milou!");

        while (true) {
            System.out.print("[L]ogin, [S]ign up, [Q]uit: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("Q")) {
                System.out.println("Bye!");
                HibernateUtil.shutdown();
                break;
            }

            switch (input) {
                case "S": signUp(); break;
                case "L":
                    Optional<User> u = login();
                    u.ifPresent(Main::dashboard);
                    break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void signUp() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        List<String> errs = new ArrayList<>();
        if (svc.register(name, email, pwd, errs)) {
            System.out.println("Your new account is created. Go ahead and login!");
        } else {
            System.out.println("Signup failed:");
            errs.forEach(err -> System.out.println(" - " + err));
        }
    }

    private static Optional<User> login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        Optional<User> user = svc.login(email, pwd);
        if (user.isEmpty()) {
            System.out.println("Invalid email or password.");
        }
        return user;
    }

    private static void dashboard(User user) {
        System.out.println("\nWelcome back, " + user.getName() + "!");

        while (true) {
            printUnread(user);
            System.out.print("\nCommands: [S]end, [V]iew, [R]eply, [F]orward, [D]elete, [T]oggle Star, View [*]Starred, [L]ogout: ");
            String cmd = scanner.nextLine().trim().toUpperCase();

            switch (cmd) {
                case "L": return;
                case "S": doSend(user); break;
                case "V": doView(user); break;
                case "R": doReply(user); break;
                case "F": doForward(user); break;
                case "D": doDelete(user); break;
                case "T": toggleStar(user); break;
                case "*": viewStarred(user); break;

                default: System.out.println("Unknown command.");
            }
        }
    }

    private static void printUnread(User user) {
        List<Email> unread = svc.getUnreadEmails(user);
        if (unread.isEmpty()) {
            System.out.println("No unread emails.");
        } else {
            System.out.println("\n" + unread.size() + " unread emails:");
            unread.forEach(e -> System.out.printf("+ %s - %s (%s)%n",
                    e.getSender().getEmail(), e.getSubject(), e.getCode()));
        }
    }

    private static void doSend(User user) {
        System.out.print("Recipients (comma separated): ");
        List<String> recs = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
        System.out.print("Subject: ");
        String subj = scanner.nextLine();
        System.out.print("Body: ");
        String body = scanner.nextLine();

        String code = svc.sendEmail(user, recs, subj, body);
        System.out.println("Successfully sent your email. Code: " + code);
    }

    private static void doView(User user) {
        System.out.print("[A]ll, [U]nread, [S]ent, Read by [C]ode: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        switch (choice) {
            case "A":
                List<Email> all = svc.getAllEmails(user);
                if (all.isEmpty()) {
                    System.out.println("No emails.");
                } else {
                    System.out.println("All Emails:");
                    all.forEach(e -> System.out.printf("+ %s - %s (%s)%n",
                            e.getSender().getEmail(), e.getSubject(), e.getCode()));
                }
                break;

            case "U":
                List<Email> unread = svc.getUnreadEmails(user);
                if (unread.isEmpty()) {
                    System.out.println("No unread emails.");
                } else {
                    System.out.println("Unread Emails:");
                    unread.forEach(e -> System.out.printf("+ %s - %s (%s)%n",
                            e.getSender().getEmail(), e.getSubject(), e.getCode()));
                }
                break;

            case "S":
                List<Email> sent = svc.getSentEmails(user);
                if (sent.isEmpty()) {
                    System.out.println("No sent emails.");
                } else {
                    System.out.println("Sent Emails:");
                    sent.forEach(e -> {
                        String recs = e.getDeliveries().stream()
                                .map(d -> d.getRecipient().getEmail())
                                .collect(Collectors.joining(", "));
                        System.out.printf("+ %s - %s (%s)%n", recs, e.getSubject(), e.getCode());
                    });
                }
                break;

            case "C":
                System.out.print("Code: ");
                String code = scanner.nextLine().trim();
                Optional<Email> emailOpt = Optional.ofNullable(svc.openEmail(user, code));

                if (emailOpt.isEmpty()) {
                    System.out.println("You cannot read this email.");
                    return;
                }

                Email email = emailOpt.get();
                String recs = email.getDeliveries().stream()
                        .map(d -> d.getRecipient().getEmail())
                        .collect(Collectors.joining(", "));

                System.out.println("Code: " + email.getCode());
                System.out.println("Recipient(s): " + recs);
                System.out.println("Subject: " + email.getSubject());
                System.out.println("Date: " + email.getSentTime());
                System.out.println("\n" + email.getBody());

                svc.openEmail(user, code);
                break;

            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void doReply(User user) {
        System.out.print("Code: ");
        String c = scanner.nextLine().trim();
        System.out.print("Body: ");
        String body = scanner.nextLine();

        String code = svc.replyEmail(user, c, body);
        if (code != null) {
            System.out.printf("Successfully sent your reply to email %s. Code: %s%n", c, code);
        } else {
            System.out.println("Email not found or not allowed.");
        }
    }

    private static void doForward(User user) {
        System.out.print("Code: ");
        String c = scanner.nextLine().trim();
        System.out.print("Recipients (comma separated): ");
        List<String> recs = Arrays.stream(scanner.nextLine().split("\\s*,\\s*"))
                .collect(Collectors.toList());

        String code = svc.forwardEmail(user, c, recs);
        if (code != null) {
            System.out.println("Successfully forwarded your email.\nCode: " + code);
        } else {
            System.out.println("Email not found.");
        }
    }

    private static void doDelete(User user) {
        System.out.print("Code of email to delete: ");
        String code = scanner.nextLine().trim();

        boolean success = svc.deleteEmail(user, code);
        if (success) {
            System.out.println("Email deleted successfully.");
        } else {
            System.out.println("Failed to delete email. Either it doesn't exist or you don't have permission.");
        }
    }

    private static void toggleStar(User user) {
        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        if (starredCodes.contains(code)) {
            starredCodes.remove(code);
            System.out.println("Unstarred email " + code);
        } else {
            starredCodes.add(code);
            System.out.println("Starred email " + code);
        }
    }

    private static void viewStarred(User user) {
        List<Email> all = svc.getAllEmails(user);
        List<Email> starred = all.stream()
                .filter(e -> starredCodes.contains(e.getCode()))
                .toList();

        if (starred.isEmpty()) {
            System.out.println("No starred emails.");
        } else {
            System.out.println("Starred Emails:");
            starred.forEach(e -> System.out.printf("+ %s - %s (%s)%n",
                    e.getSender().getEmail(), e.getSubject(), e.getCode()));
        }
    }

}
