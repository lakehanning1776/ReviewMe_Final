package com.example.demo.controller;

import com.example.demo.model.CreatorProfile;
import com.example.demo.model.CreatorReview;
import com.example.demo.model.CreatorCode;
import com.example.demo.model.Game;
import com.example.demo.model.User;
import com.example.demo.model.CreatorFollow;
import com.example.demo.model.ContentReport;
import com.example.demo.repository.CreatorFollowRepository;
import com.example.demo.repository.ContentReportRepository;
import com.example.demo.repository.CreatorProfileRepository;
import com.example.demo.repository.CreatorReviewRepository;
import com.example.demo.repository.CreatorCodeRepository;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountDashboardController {
    private final UserRepository userRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final CreatorReviewRepository creatorReviewRepository;
    private final CreatorCodeRepository creatorCodeRepository;
    private final GameRepository gameRepository;
    private final CreatorFollowRepository creatorFollowRepository;
    private final ContentReportRepository contentReportRepository;

    public AccountDashboardController(UserRepository userRepository,
                                      CreatorProfileRepository creatorProfileRepository,
                                      CreatorReviewRepository creatorReviewRepository,
                                      CreatorCodeRepository creatorCodeRepository,
                                      GameRepository gameRepository, CreatorFollowRepository creatorFollowRepository,
                                      ContentReportRepository contentReportRepository) {
        this.userRepository = userRepository;
        this.creatorProfileRepository = creatorProfileRepository;
        this.creatorReviewRepository = creatorReviewRepository;
        this.creatorCodeRepository = creatorCodeRepository;
        this.gameRepository = gameRepository;
        this.creatorFollowRepository = creatorFollowRepository;
        this.contentReportRepository = contentReportRepository;
    }
    private Long sessionUserId(HttpSession session) {
        Object value = session.getAttribute(AuthController.SESSION_USER_ID);
        return value instanceof Long id ? id : null;
    }
    private String role(HttpSession session) { Object r = session.getAttribute(AuthController.SESSION_ROLE); return r == null ? "" : r.toString(); }

    @GetMapping("/account")
    public String myAccount(HttpSession session, Model model) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/account";
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) { session.invalidate(); return "redirect:/login"; }
        model.addAttribute("accountUser", user);
        model.addAttribute("creatorProfile", creatorProfileRepository.findByUserId(userId).orElse(null));
        return "my_account";
    }

    @GetMapping("/creator")
    public String creatorDashboard(HttpSession session, Model model, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creator";
        if (!"CREATOR".equals(role(session)) && !"ADMIN".equals(role(session))) {
            flash.addFlashAttribute("dashboardError", "Your account is a regular user. An admin must grant the Creator role first.");
            return "redirect:/";
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) { session.invalidate(); return "redirect:/login"; }
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId).orElseGet(() -> {
            CreatorProfile p = new CreatorProfile(); p.setUser(user); p.setChannelName(user.getUsername()); return p;
        });
        model.addAttribute("profile", profile);
        model.addAttribute("games", gameRepository.findAll());
        model.addAttribute("creatorReviews", creatorReviewRepository.findByCreatorIdOrderByCreatedAtDesc(profile.getId() == null ? -1L : profile.getId()));
        model.addAttribute("creatorCodes", creatorCodeRepository.findByCreatorIdOrderByCreatedAtDesc(profile.getId() == null ? -1L : profile.getId()));
        model.addAttribute("openReports", contentReportRepository.findByStatusOrderByCreatedAtDesc("OPEN"));
        return "creator_dashboard";
    }

    @PostMapping("/creator/profile")
    public String saveCreatorProfile(@RequestParam String channelName, @RequestParam(required=false) String channelUrl,
                                     @RequestParam(required=false) String bio, HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creator";
        if (!"CREATOR".equals(role(session)) && !"ADMIN".equals(role(session))) return "redirect:/";
        String name = channelName == null ? "" : channelName.trim();
        if (name.isBlank() || name.length() > 150 || (channelUrl != null && channelUrl.length() > 500)) {
            flash.addFlashAttribute("dashboardError", "Enter a channel name (150 characters max) and a valid-length channel URL."); return "redirect:/creator";
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) { session.invalidate(); return "redirect:/login"; }
        CreatorProfile p = creatorProfileRepository.findByUserId(userId).orElseGet(CreatorProfile::new);
        p.setUser(user); p.setChannelName(name); p.setChannelUrl(channelUrl == null ? null : channelUrl.trim()); p.setBio(bio == null ? null : bio.trim());
        creatorProfileRepository.save(p);
        flash.addFlashAttribute("dashboardSuccess", "Creator profile saved."); return "redirect:/creator";
    }

    @PostMapping("/creator/reviews")
    public String saveCreatorReview(@RequestParam Long gameId,
                                    @RequestParam String videoUrl,
                                    @RequestParam BigDecimal rating,
                                    @RequestParam(required = false) String reviewText,
                                    @RequestParam(defaultValue = "NONE") String disclosureType,
                                    @RequestParam(required = false) String disclosureDetails,
                                    HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creator";
        if (!"CREATOR".equals(role(session)) && !"ADMIN".equals(role(session))) return "redirect:/";
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId).orElse(null);
        Game game = gameRepository.findById(gameId).orElse(null);
        String url = videoUrl == null ? "" : videoUrl.trim();
        String text = reviewText == null ? "" : reviewText.trim();
        if (profile == null) {
            flash.addFlashAttribute("dashboardError", "Save your creator profile before posting a video review.");
            return "redirect:/creator";
        }
        if (game == null || url.isBlank() || url.length() > 500 || !(url.startsWith("https://") || url.startsWith("http://")) || rating == null || rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.TEN) > 0 || rating.scale() > 1 || text.length() > 5000) {
            flash.addFlashAttribute("dashboardError", "Choose a game, enter a valid video URL, give a score from 1 to 10 (half-points allowed), and keep the review under 5,000 characters.");
            return "redirect:/creator";
        }
        CreatorReview review = new CreatorReview();
        review.setCreator(profile);
        review.setGame(game);
        review.setVideoUrl(url);
        review.setRating(rating.setScale(1, java.math.RoundingMode.HALF_UP));
        review.setReviewText(text.isBlank() ? null : text);
        if (!java.util.Set.of("NONE", "PAID", "FREE_PRODUCT", "AFFILIATE", "OTHER").contains(disclosureType)) {
            flash.addFlashAttribute("dashboardError", "Choose a valid sponsorship disclosure."); return "redirect:/creator#creator-reviews";
        }
        String details = disclosureDetails == null ? "" : disclosureDetails.trim();
        if (!"NONE".equals(disclosureType) && details.isBlank()) {
            flash.addFlashAttribute("dashboardError", "Add details for sponsored, gifted, affiliate, or other relationships."); return "redirect:/creator#creator-reviews";
        }
        if (details.length() > 2000) { flash.addFlashAttribute("dashboardError", "Disclosure details must be under 2,000 characters."); return "redirect:/creator#creator-reviews"; }
        review.setSponsored(!"NONE".equals(disclosureType));
        review.setDisclosureType(disclosureType);
        review.setDisclosureDetails(details.isBlank() ? null : details);
        review.setUpdatedAt(java.time.LocalDateTime.now());
        creatorReviewRepository.save(review);
        flash.addFlashAttribute("dashboardSuccess", "Your creator video review and separate score were saved.");
        return "redirect:/creator#creator-reviews";
    }

    @PostMapping("/creator/codes")
    public String saveCreatorCode(@RequestParam(required = false) Long gameId,
                                  @RequestParam String code,
                                  @RequestParam(required = false) String storeUrl,
                                  @RequestParam(required = false) String discountDescription,
                                  @RequestParam(required = false) String expiresAt,
                                  @RequestParam(required = false) String affiliateDisclosure,
                                  HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creator";
        if (!"CREATOR".equals(role(session)) && !"ADMIN".equals(role(session))) return "redirect:/";
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId).orElse(null);
        String cleanCode = code == null ? "" : code.trim();
        String cleanUrl = storeUrl == null ? "" : storeUrl.trim();
        String description = discountDescription == null ? "" : discountDescription.trim();
        String disclosure = affiliateDisclosure == null ? "" : affiliateDisclosure.trim();
        Game game = null;
        if (gameId != null) {
            game = gameRepository.findById(gameId).orElse(null);
            if (game == null) { flash.addFlashAttribute("dashboardError", "Selected game was not found."); return "redirect:/creator#discount-codes"; }
        }
        LocalDate expiry = null;
        try { if (expiresAt != null && !expiresAt.isBlank()) expiry = LocalDate.parse(expiresAt); }
        catch (Exception e) { flash.addFlashAttribute("dashboardError", "Enter a valid expiration date."); return "redirect:/creator#discount-codes"; }
        if (profile == null) { flash.addFlashAttribute("dashboardError", "Save your creator profile before adding codes."); return "redirect:/creator"; }
        if (cleanCode.isBlank() || cleanCode.length() > 100 || cleanUrl.length() > 500 || (!cleanUrl.isBlank() && !(cleanUrl.startsWith("https://") || cleanUrl.startsWith("http://"))) || description.length() > 255 || disclosure.length() > 5000) {
            flash.addFlashAttribute("dashboardError", "Enter a code (100 characters max), a valid optional store URL, and a short discount description.");
            return "redirect:/creator#discount-codes";
        }
        CreatorCode creatorCode = new CreatorCode();
        creatorCode.setCreator(profile);
        creatorCode.setGame(game);
        creatorCode.setCode(cleanCode);
        creatorCode.setStoreUrl(cleanUrl.isBlank() ? null : cleanUrl);
        creatorCode.setDiscountDescription(description.isBlank() ? null : description);
        creatorCode.setExpiresAt(expiry);
        creatorCode.setAffiliateDisclosure(disclosure.isBlank() ? null : disclosure);
        creatorCodeRepository.save(creatorCode);
        flash.addFlashAttribute("dashboardSuccess", "Discount code saved.");
        return "redirect:/creator#discount-codes";
    }

    @PostMapping("/creator/codes/{id}/delete")
    public String deleteCreatorCode(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creator";
        if (!"CREATOR".equals(role(session)) && !"ADMIN".equals(role(session))) return "redirect:/";
        CreatorProfile profile = creatorProfileRepository.findByUserId(userId).orElse(null);
        CreatorCode code = creatorCodeRepository.findById(id).orElse(null);
        if (profile == null || code == null || !code.getCreator().getId().equals(profile.getId())) {
            flash.addFlashAttribute("dashboardError", "Discount code not found for your account.");
        } else {
            creatorCodeRepository.delete(code);
            flash.addFlashAttribute("dashboardSuccess", "Discount code deleted.");
        }
        return "redirect:/creator#discount-codes";
    }

    @GetMapping("/creators/{id}")
    public String publicCreator(@PathVariable Long id, HttpSession session, Model model) {
        CreatorProfile profile = creatorProfileRepository.findById(id).orElse(null);
        if (profile == null) return "redirect:/";
        Long userId = sessionUserId(session);
        model.addAttribute("profile", profile);
        model.addAttribute("creatorReviews", creatorReviewRepository.findByCreatorIdOrderByCreatedAtDesc(id));
        model.addAttribute("creatorCodes", creatorCodeRepository.findByCreatorIdOrderByCreatedAtDesc(id).stream().filter(c -> c.getExpiresAt() == null || !c.getExpiresAt().isBefore(LocalDate.now())).toList());
        model.addAttribute("followerCount", creatorFollowRepository.countByCreatorId(id));
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("following", userId != null && creatorFollowRepository.existsByUserIdAndCreatorId(userId, id));
        return "creator_public";
    }

    @GetMapping("/following")
    public String followingFeed(HttpSession session, Model model) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/following";
        var follows = creatorFollowRepository.findByUserIdOrderByCreatedAtDesc(userId);
        var profiles = follows.stream().map(CreatorFollow::getCreator).toList();
        var reviews = profiles.stream().flatMap(profile -> creatorReviewRepository.findByCreatorIdOrderByCreatedAtDesc(profile.getId()).stream()).sorted((a,b) -> {
            if (a.getCreatedAt() == null) return 1; if (b.getCreatedAt() == null) return -1; return b.getCreatedAt().compareTo(a.getCreatedAt());
        }).toList();
        model.addAttribute("followedCreators", profiles);
        model.addAttribute("feedReviews", reviews);
        return "following_feed";
    }

    @PostMapping("/creators/{id}/follow")
    public String toggleFollow(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login?next=/creators/" + id;
        CreatorProfile profile = creatorProfileRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (profile == null || user == null || profile.getUser().getId().equals(userId)) return "redirect:/creators/" + id;
        var existing = creatorFollowRepository.findByUserIdAndCreatorId(userId, id);
        if (existing.isPresent()) creatorFollowRepository.delete(existing.get());
        else { CreatorFollow follow = new CreatorFollow(); follow.setUser(user); follow.setCreator(profile); creatorFollowRepository.save(follow); }
        return "redirect:/creators/" + id;
    }

    @PostMapping("/reports")
    public String submitReport(@RequestParam String targetType, @RequestParam Long targetId,
                               @RequestParam String reason, @RequestParam(required=false) String details,
                               HttpSession session, RedirectAttributes flash) {
        Long userId = sessionUserId(session);
        if (userId == null) return "redirect:/login";
        String type = targetType == null ? "" : targetType.trim().toUpperCase(java.util.Locale.ROOT);
        String why = reason == null ? "" : reason.trim();
        if (!java.util.Set.of("REVIEW", "CODE").contains(type) || targetId == null || why.isBlank() || why.length() > 80 || (details != null && details.length() > 2000)) {
            flash.addFlashAttribute("reportMessage", "Please provide a valid report reason."); return "redirect:/";
        }
        boolean exists = "REVIEW".equals(type) ? creatorReviewRepository.existsById(targetId) : creatorCodeRepository.existsById(targetId);
        if (!exists) { flash.addFlashAttribute("reportMessage", "That item could not be found."); return "redirect:/"; }
        ContentReport report = new ContentReport(); report.setReporter(userRepository.findById(userId).orElseThrow());
        report.setTargetType(type); report.setTargetId(targetId); report.setReason(why); report.setDetails(details == null || details.isBlank() ? null : details.trim());
        contentReportRepository.save(report);
        flash.addFlashAttribute("reportMessage", "Thanks. Your report was sent to the moderation queue.");
        return "redirect:/";
    }

    @PostMapping("/admin/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (sessionUserId(session) == null || !"ADMIN".equals(role(session))) return "redirect:/";
        ContentReport report = contentReportRepository.findById(id).orElse(null);
        if (report != null) { report.setStatus("RESOLVED"); report.setResolvedAt(java.time.LocalDateTime.now()); contentReportRepository.save(report); }
        flash.addFlashAttribute("dashboardSuccess", "Report marked resolved."); return "redirect:/admin#moderation";
    }

    @PostMapping("/admin/codes/{id}/verify")
    public String verifyCode(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (sessionUserId(session) == null || !"ADMIN".equals(role(session))) return "redirect:/";
        CreatorCode code = creatorCodeRepository.findById(id).orElse(null);
        if (code != null) { code.setVerified(true); code.setLastVerifiedAt(java.time.LocalDateTime.now()); creatorCodeRepository.save(code); }
        flash.addFlashAttribute("dashboardSuccess", "Discount code marked verified."); return "redirect:/admin#moderation";
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes flash) {
        if (sessionUserId(session) == null) return "redirect:/login?next=/admin";
        if (!"ADMIN".equals(role(session))) { flash.addFlashAttribute("dashboardError", "Admin access is required."); return "redirect:/"; }
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("openReports", contentReportRepository.findByStatusOrderByCreatedAtDesc("OPEN"));
        model.addAttribute("unverifiedCodes", creatorCodeRepository.findAll().stream().filter(c -> !Boolean.TRUE.equals(c.getVerified()) && (c.getExpiresAt() == null || !c.getExpiresAt().isBefore(LocalDate.now()))).toList());
        return "admin_dashboard";
    }

    @PostMapping("/admin/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam String role, HttpSession session, RedirectAttributes flash) {
        if (sessionUserId(session) == null) return "redirect:/login?next=/admin";
        if (!"ADMIN".equals(role(session))) return "redirect:/";
        if (!java.util.Set.of("USER", "CREATOR", "ADMIN").contains(role)) {
            flash.addFlashAttribute("dashboardError", "Invalid role selected."); return "redirect:/admin";
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null) { flash.addFlashAttribute("dashboardError", "Account not found."); return "redirect:/admin"; }
        Long actingId = sessionUserId(session);
        if (target.getId().equals(actingId) && !"ADMIN".equals(role)) {
            flash.addFlashAttribute("dashboardError", "You cannot remove your own admin role here."); return "redirect:/admin";
        }
        target.setRole(role); userRepository.save(target);
        if (target.getId().equals(actingId)) session.setAttribute(AuthController.SESSION_ROLE, role);
        flash.addFlashAttribute("dashboardSuccess", "Updated " + target.getUsername() + " to " + role + "."); return "redirect:/admin";
    }
}
