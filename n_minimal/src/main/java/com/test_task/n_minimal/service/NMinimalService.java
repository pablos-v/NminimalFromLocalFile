package com.test_task.n_minimal.service;

import com.test_task.n_minimal.exception.LinkNotFoundException;
import com.test_task.n_minimal.exception.LinkProcessingException;
import com.test_task.n_minimal.exception.ValueNNotFoundException;
import com.test_task.n_minimal.exception.ValueNProcessingException;

public interface NMinimalService {
    /**
     * Отдаёт N число из локального файла, переданного в первом параметре.
     * Значения берутся из 1 столбца 1 листа, и сортируются по возрастанию.
     * @param fileLink ссылка на локальный файл
     * @param N        требуемое минимальное число
     * @return значение N-го минимального числа
     * @throws ValueNNotFoundException   если N не передано
     * @throws ValueNProcessingException если N не валидно
     * @throws LinkNotFoundException     если ссылка не передана
     * @throws LinkProcessingException   если ссылка не валидна
     */
    Long getNthMinimal(final String fileLink, final String N) throws LinkNotFoundException, LinkProcessingException
            , ValueNNotFoundException, ValueNProcessingException;

}
