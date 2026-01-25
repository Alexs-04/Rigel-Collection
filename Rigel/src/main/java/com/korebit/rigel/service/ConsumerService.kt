package com.korebit.rigel.service

import com.korebit.rigel.dto.ConsumerDTO
import com.korebit.rigel.model.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.stereotype.Service

@Service
class ConsumerService(private val consumerRepository: ConsumerRepository) {

    fun saveConsumer(consumerDTO: ConsumerDTO): Consumer {

        if(consumerRepository.existsByUsername(consumerDTO.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        val consumer = Consumer(
            name = consumerDTO.name,
            role = consumerDTO.role,
            username = consumerDTO.username,
            password = consumerDTO.password,
            email = consumerDTO.email,
            phoneNumber = consumerDTO.phoneNumber
        )
        return consumerRepository.save(consumer)
    }
}