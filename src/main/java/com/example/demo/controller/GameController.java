package com.example.demo.controller;

import com.example.demo.model.Game;
import com.example.demo.model.Review;
import com.example.demo.model.GameTag;
import com.example.demo.model.User;
import com.example.demo.model.CreatorReview;
import com.example.demo.model.CreatorCode;
import jakarta.servlet.http.HttpSession;
import com.example.demo.repository.GameTagRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.CreatorReviewRepository;
import com.example.demo.repository.CreatorCodeRepository;
import com.example.demo.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class GameController {

    private final GameService gameService;
    private final ReviewRepository reviewRepository;
    private final GameTagRepository gameTagRepository;
    private final com.example.demo.repository.UserRepository userRepository;
    private final CreatorReviewRepository creatorReviewRepository;
    private final CreatorCodeRepository creatorCodeRepository;
    private final com.example.demo.repository.CreatorProfileRepository creatorProfileRepository;

    public GameController(GameService gameService, ReviewRepository reviewRepository, GameTagRepository gameTagRepository,
                         com.example.demo.repository.UserRepository userRepository,
                         CreatorReviewRepository creatorReviewRepository, CreatorCodeRepository creatorCodeRepository,
                         com.example.demo.repository.CreatorProfileRepository creatorProfileRepository) {
        this.gameService = gameService;
        this.reviewRepository = reviewRepository;
        this.gameTagRepository = gameTagRepository;
        this.userRepository = userRepository;
        this.creatorReviewRepository = creatorReviewRepository;
        this.creatorCodeRepository = creatorCodeRepository;
        this.creatorProfileRepository = creatorProfileRepository;
    }

    @GetMapping("/")
    public String viewHomePage(@RequestParam(required = false) String tag, Model model) {
        List<Game> allGames = gameService.getAllGames();
        String selectedTag = tag == null ? "" : tag.trim();
        List<Game> games = selectedTag.isBlank() ? allGames : allGames.stream()
                .filter(game -> game.getTags().stream().anyMatch(t -> t.getNormalizedName().equals(selectedTag.toLowerCase(java.util.Locale.ROOT))))
                .toList();
        model.addAttribute("selectedTag", selectedTag);
        model.addAttribute("popularTags", allGames.stream().flatMap(game -> game.getTags().stream())
                .collect(java.util.stream.Collectors.groupingBy(GameTag::getName, java.util.stream.Collectors.counting()))
                .entrySet().stream().sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(12).toList());
        model.addAttribute("listGames", games);
        model.addAttribute("featuredCreators", creatorProfileRepository.findAll().stream().limit(8).toList());
        model.addAttribute("reviewCounts", games.stream().collect(java.util.stream.Collectors.toMap(
                Game::getId, game -> reviewRepository.countByGameId(game.getId()))));
        model.addAttribute("ratingAverages", games.stream().collect(java.util.stream.Collectors.toMap(
                Game::getId, game -> {
                    List<Review> reviews = reviewRepository.findByGameIdOrderByCreatedAtDesc(game.getId());
                    return reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
                })));
        return "index";
    }

    @GetMapping("/games/{id}")
    public String viewGame(@PathVariable Long id, Model model) {
        Game game = gameService.getGameById(id);
        List<Review> reviews = reviewRepository.findByGameIdOrderByCreatedAtDesc(id);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        model.addAttribute("game", game);
        model.addAttribute("loggedInUsername", org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() instanceof org.springframework.web.context.request.ServletRequestAttributes attrs ? attrs.getRequest().getSession(false) == null ? null : attrs.getRequest().getSession(false).getAttribute(com.example.demo.controller.AuthController.SESSION_USERNAME) : null);
        model.addAttribute("reviews", reviews);
        model.addAttribute("review", new Review());
        model.addAttribute("averageRating", average);
        model.addAttribute("reviewCount", reviews.size());
        List<CreatorReview> creatorReviews = creatorReviewRepository.findByGameIdOrderByCreatedAtDesc(id);
        double creatorAverage = creatorReviews.stream().map(CreatorReview::getRating).filter(java.util.Objects::nonNull).mapToDouble(BigDecimal -> BigDecimal.doubleValue()).average().orElse(0.0);
        model.addAttribute("creatorReviews", creatorReviews);
        model.addAttribute("creatorReviewCount", creatorReviews.size());
        model.addAttribute("creatorAverageRating", creatorAverage);
        model.addAttribute("creatorCodes", creatorCodeRepository.findByGameIdOrderByCreatedAtDesc(id).stream().filter(c -> c.getExpiresAt() == null || !c.getExpiresAt().isBefore(java.time.LocalDate.now())).toList());
        model.addAttribute("allTags", gameTagRepository.findAll().stream().sorted(java.util.Comparator.comparing(GameTag::getName, String.CASE_INSENSITIVE_ORDER)).toList());
        return "game_detail";
    }


    @PostMapping("/games/{id}/tags")
    public String addTag(@PathVariable Long id, @RequestParam String tagName, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("tagError", "Please log in before adding a community tag.");
            return "redirect:/login?next=/games/" + id;
        }
        Game game = gameService.getGameById(id);
        String name = tagName == null ? "" : tagName.trim().replaceAll("\\s+", " ");
        if (name.length() < 2 || name.length() > 32 || !name.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} &'-]*")) {
            redirectAttributes.addFlashAttribute("tagError", "Use 2–32 characters: letters, numbers, spaces, hyphens, apostrophes, or &.");
            return "redirect:/games/" + id + "#community-tags";
        }
        String normalized = name.toLowerCase(java.util.Locale.ROOT);
        GameTag tag = gameTagRepository.findByNormalizedName(normalized).orElseGet(() -> gameTagRepository.save(new GameTag(name)));
        boolean added = game.getTags().add(tag);
        if (added) {
            gameService.saveGame(game);
            redirectAttributes.addFlashAttribute("tagSuccess", "Tag added to this game.");
        } else {
            redirectAttributes.addFlashAttribute("tagSuccess", "That tag is already on this game.");
        }
        return "redirect:/games/" + id + "#community-tags";
    }

    @PostMapping("/games/{id}/reviews")
    public String submitReview(@PathVariable Long id,
                               @RequestParam Integer rating,
                               @RequestParam String reviewText,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Object userIdValue = session.getAttribute(AuthController.SESSION_USER_ID);
        if (!(userIdValue instanceof Long userId)) {
            redirectAttributes.addFlashAttribute("reviewError", "Please log in or create an account before reviewing.");
            return "redirect:/login?next=/games/" + id + "#write-review";
        }
        Game game = gameService.getGameById(id);
        User user = userRepository.findById(userId).orElse(null);
        String text = reviewText == null ? "" : reviewText.trim();
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        if (rating == null || rating < 1 || rating > 5 || text.isBlank() || text.length() > 2000) {
            redirectAttributes.addFlashAttribute("reviewError", "Choose 1–5 stars and write a review of up to 2,000 characters.");
            return "redirect:/games/" + id + "#write-review";
        }
        Review review = reviewRepository.findByGameIdAndAuthorId(id, userId).orElseGet(Review::new);
        review.setGame(game);
        review.setAuthor(user);
        review.setReviewerName(user.getUsername());
        review.setRating(rating);
        review.setReviewText(text);
        if (review.getCreatedAt() == null) review.setCreatedAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("reviewSuccess", "Your review was saved to your account.");
        return "redirect:/games/" + id + "#reviews";
    }

    private boolean isLoggedIn(HttpSession session) {
        Object value = session.getAttribute(AuthController.SESSION_USER_ID);
        return value instanceof Long userId && userRepository.existsById(userId);
    }

    private boolean canManageGames(HttpSession session) {
        Object value = session.getAttribute(AuthController.SESSION_USER_ID);
        if (!(value instanceof Long userId)) return false;
        return userRepository.findById(userId)
                .map(user -> "ADMIN".equals(user.getRole()) || "CREATOR".equals(user.getRole()))
                .orElse(false);
    }

    @GetMapping("/showNewGameForm")
    public String createGameForm(HttpSession session, Model model, RedirectAttributes flash) {
        if (!canManageGames(session)) {
            flash.addFlashAttribute("dashboardError", "Only administrators and content creators can add games.");
            return "redirect:/";
        }
        model.addAttribute("game", new Game());
        return "new_game";
    }

    @PostMapping("/saveGame")
    public String saveGame(@ModelAttribute("game") Game game, HttpSession session, RedirectAttributes flash) {
        if (!canManageGames(session)) {
            flash.addFlashAttribute("dashboardError", "Only administrators and content creators can add or edit games.");
            return "redirect:/";
        }
        gameService.saveGame(game);
        return "redirect:/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes flash) {
        if (!canManageGames(session)) {
            flash.addFlashAttribute("dashboardError", "Only administrators and content creators can edit games.");
            return "redirect:/";
        }
        model.addAttribute("game", gameService.getGameById(id));
        return "update_game";
    }

    @PostMapping("/deleteGame/{id}")
    public String deleteGame(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!canManageGames(session)) {
            flash.addFlashAttribute("dashboardError", "Only administrators and content creators can delete games.");
            return "redirect:/";
        }
        reviewRepository.deleteAll(reviewRepository.findByGameIdOrderByCreatedAtDesc(id));
        gameService.deleteGame(id);
        return "redirect:/";
    }
}
