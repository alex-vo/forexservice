package com.example.forexservice.controller

import com.example.forexservice.exception.EntityNotFoundException
import com.example.forexservice.exception.ForexServiceValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ClientExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ForexServiceValidationException::class)
    @ResponseBody
    fun handleForexServiceValidationException(e: ForexServiceValidationException): Map<String, String?> {
        return mapOf("message" to e.message)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseBody
    fun handleEntityNotFoundException(e: EntityNotFoundException): Map<String, String?> {
        return mapOf("message" to e.message)
    }

}