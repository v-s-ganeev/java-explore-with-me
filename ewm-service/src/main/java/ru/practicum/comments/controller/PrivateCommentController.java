package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                 @PathVariable Long userId,
                                 @RequestParam Long eventId) {
        return commentService.create(newCommentDto, userId, eventId);

    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long commentId) {
        return commentService.update(newCommentDto, userId, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentService.deleteByAuthor(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentService.getUserComments(userId, pageRequest);
    }

}
