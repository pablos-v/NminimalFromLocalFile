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

    @Override
    public Long getNthMinimal(String fileLink, String N) throws LinkNotFoundException, LinkProcessingException,
            ValueNNotFoundException, ValueNProcessingException {

        Validator.validateInput(fileLink, N);
        List<Long> unsorted = XlsxToListConverter.convert(fileLink);

        Validator.validateNWithListSize(unsorted);
        List<Long> sorted = Sorter.sort(unsorted);

        return sorted.get(Validator.VALUE_N-1);
    }
}
