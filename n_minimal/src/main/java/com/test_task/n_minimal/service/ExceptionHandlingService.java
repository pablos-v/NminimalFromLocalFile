package com.test_task.n_minimal.service;


import com.test_task.n_minimal.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Класс обработки выбрасываемых исключений
 */
@ControllerAdvice
public class ExceptionHandlingService {

    private final Logger logger = LoggerFactory.getLogger(ExceptionHandlingService.class);

    /**
     * Метод обрабатывает исключения {@link LinkProcessingException}, {@link ValueNProcessingException},
     * возникающие в процессе валидации.
     *
     * @param e выбрасываемое исключение
     * @return статус ответа 400 и текст ошибки
     */
    @ResponseBody
    @ExceptionHandler({LinkProcessingException.class, ValueNProcessingException.class, FileProcessingException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleProcessingException(Exception e) {
        logger.error(e.getMessage(), e);
        return e.getMessage();
    }

    /**
     * Метод обрабатывает исключения {@link LinkNotFoundException}, {@link ValueNNotFoundException},
     * возникающие когда ссылка или значение N не переданы.
     *
     * @param e выбрасываемое исключение
     * @return статус ответа 404 и текст ошибки
     */
    @ResponseBody
    @ExceptionHandler({LinkNotFoundException.class, ValueNNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(Exception e) {
        logger.error(e.getMessage(), e);
        return e.getMessage();
    }
}
