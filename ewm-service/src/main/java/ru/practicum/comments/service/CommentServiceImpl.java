package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    @Override
    @Transactional
    public CommentDto create(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Нельзя оставить комментарий у события которое еще не опубликовано.");
        }
        Comment comment = Comment.builder()
                .author(user)
                .event(event)
                .message(newCommentDto.getMessage())
                .created(LocalDateTime.now())
                .build();
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto update(NewCommentDto newCommentDto, Long userId, Long commentId) {
        User user = checkUser(userId);
        Comment comment = checkComment(commentId);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Изменять комментарий может только его автор");
        }
        comment.setMessage(newCommentDto.getMessage());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteByAuthor(Long userId, Long commentId) {
        User user = checkUser(userId);
        Comment comment = checkComment(commentId);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Вы не являетесь автором комментария, поэтому не можете его удалить");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        checkComment(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, PageRequest pageRequest) {
        checkEvent(eventId);
        return commentRepository.findAllByEvent_IdOrderByCreatedDesc(eventId, pageRequest).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, PageRequest pageRequest) {
        checkUser(userId);
        return commentRepository.findAllByAuthor_IdOrderByCreatedDesc(userId, pageRequest).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsForAdmin(LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Начала периода не может быть раньше его конца");
        }
        return commentRepository.getCommentsByAdmin(rangeStart, rangeEnd, pageRequest).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Comment checkComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден."));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
    }
}
