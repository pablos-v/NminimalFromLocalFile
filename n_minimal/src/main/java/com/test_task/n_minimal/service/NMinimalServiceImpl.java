package com.test_task.n_minimal.service;

import com.test_task.n_minimal.exception.LinkNotFoundException;
import com.test_task.n_minimal.exception.LinkProcessingException;
import com.test_task.n_minimal.exception.ValueNNotFoundException;
import com.test_task.n_minimal.exception.ValueNProcessingException;
import com.test_task.n_minimal.util.Sorter;
import com.test_task.n_minimal.util.Validator;
import com.test_task.n_minimal.util.XlsxToListConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NMinimalServiceImpl implements NMinimalService {

    private final Validator validator;
    private final XlsxToListConverter converter;
    private final Sorter sorter;

    public NMinimalServiceImpl(Validator validator, XlsxToListConverter converter, Sorter sorter) {
        this.validator = validator;
        this.converter = converter;
        this.sorter = sorter;
    }

    @Override
    public Long getNthMinimal(String fileLink, String N) throws LinkNotFoundException, LinkProcessingException,
            ValueNNotFoundException, ValueNProcessingException {

        validator.validateInput(fileLink, N);
        List<Long> unsorted = converter.convert(fileLink);

        int valueN = Integer.parseInt(N);
        validator.validateNWithListSize(unsorted, valueN);
        List<Long> sorted = sorter.sort(unsorted);

        return sorted.get(valueN-1);
    }
}
