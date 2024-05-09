package ru.practicum.comments.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {

    CommentDto create(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto update(NewCommentDto newCommentDto, Long userId, Long commentId);

    void deleteByAuthor(Long userId, Long commentId);

    void deleteByAdmin(Long commentId);

    List<CommentDto> getEventComments(Long eventId, PageRequest pageRequest);

    List<CommentDto> getUserComments(Long userId, PageRequest pageRequest);

    List<CommentDto> getCommentsForAdmin(LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);
}
