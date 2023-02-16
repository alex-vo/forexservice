package com.example.forexservice.exception

class EntityNotFoundException(message: String) : RuntimeException(message)

class ForexServiceValidationException(message: String) : RuntimeException(message)