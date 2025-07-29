package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.ChatRoom;
import com.mb.reddit.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public Optional<String> getChatRoomId(
            String senderId,
            String recipientId,
            boolean createNewRoomIfNotExists
    ) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId);

        if (chatRoomOptional.isPresent()) {
            return Optional.of(chatRoomOptional.get().getChatId());
        } else {
            if (createNewRoomIfNotExists) {
                String chatId = createChatId(senderId, recipientId);
                return Optional.of(chatId);
            } else {
                return Optional.empty();
            }
        }
    }

    private String createChatId(String senderName, String recipientName) {
        String chatId = String.format("%s_%s", senderName, recipientName);

        ChatRoom senderRecipient = new ChatRoom();
        senderRecipient.setChatId(chatId);
        senderRecipient.setSenderId(senderName);
        senderRecipient.setRecipientId(recipientName);

        ChatRoom recipientSender = new ChatRoom();
        recipientSender.setChatId(chatId);
        recipientSender.setSenderId(recipientName);
        recipientSender.setRecipientId(senderName);

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }
}