package com.tcs.transactionService.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.tcs.transactionService.dto.TransactionDto;
import com.tcs.transactionService.dto.WalletDto;
import com.tcs.transactionService.feignClient.FraudClient;
import com.tcs.transactionService.feignClient.NotificationClient;
import com.tcs.transactionService.feignClient.WalletClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletClient walletClient;
    private final FraudClient fraudClient;
    private final NotificationClient notificationClient;
    
    public TransactionDto addMoney(TransactionDto req) {

        try {
            // 1️⃣ Call Wallet Service Credit
            WalletDto walletReq = new WalletDto();
            walletReq.setUserId(req.getReceiverUserId());
            walletReq.setAmount(req.getAmount());

            WalletDto walletResponse = walletClient.credit(walletReq);

            // 2️⃣ Send Notifications
            sendNotifications(
                    req.getReceiverUserId(),
                    "Money Added Successfully",
                    "₹" + req.getAmount() + " added to your wallet. New Balance = " + walletResponse.getBalance()
            );

            // 3️⃣ Build Response
            TransactionDto dto = new TransactionDto();
            dto.setReceiverUserId(req.getReceiverUserId());
            dto.setAmount(req.getAmount());
            dto.setStatus("SUCCESS");
            dto.setMessage("Money added to wallet successfully");
            return dto;

        } catch (Exception e) {

            sendNotifications(
                    req.getReceiverUserId(),
                    "Add Money Failed",
                    "Failed to add ₹" + req.getAmount() + " due to system issue."
            );

            TransactionDto dto = new TransactionDto();
            dto.setReceiverUserId(req.getReceiverUserId());
            dto.setAmount(req.getAmount());
            dto.setStatus("FAILED");
            dto.setMessage("Add money failed: " + e.getMessage());
            return dto;
        }
    }

    public TransactionDto transfer(TransactionDto req) {

        // 1️⃣ FRAUD CHECK
        var fraudResult = fraudClient.check(
                Map.of(
                        "walletId", req.getSenderUserId(),
                        "amount", req.getAmount()
                )
        );

        // ❌ FRAUD BLOCKED
        if ("BLOCK".equals(fraudResult.get("decision"))) {

            walletClient.block(req.getSenderUserId());

            sendNotifications(
                    req.getSenderUserId(),
                    "Transaction Blocked due to Fraud",
                    "Suspicious activity detected. Wallet blocked."
            );

            TransactionDto dto = new TransactionDto();
            dto.setSenderUserId(req.getSenderUserId());
            dto.setReceiverUserId(req.getReceiverUserId());
            dto.setAmount(req.getAmount());
            dto.setStatus("BLOCKED");
            dto.setMessage(fraudResult.get("reason"));
            return dto;
        }

        // ✅ FRAUD ALLOWED → PROCESS PAYMENT
        try {
            WalletDto debitReq = new WalletDto();
            debitReq.setUserId(req.getSenderUserId());
            debitReq.setAmount(req.getAmount());
            walletClient.debit(debitReq);

            WalletDto creditReq = new WalletDto();
            creditReq.setUserId(req.getReceiverUserId());
            creditReq.setAmount(req.getAmount());
            walletClient.credit(creditReq);

            sendNotifications(
                    req.getSenderUserId(),
                    "Amount Debited Successfully",
                    "₹" + req.getAmount() + " sent to " + req.getReceiverUserId()
            );

            sendNotifications(
                    req.getReceiverUserId(),
                    "Amount Credited Successfully",
                    "₹" + req.getAmount() + " received from " + req.getSenderUserId()
            );

            TransactionDto dto = new TransactionDto();
            dto.setSenderUserId(req.getSenderUserId());
            dto.setReceiverUserId(req.getReceiverUserId());
            dto.setAmount(req.getAmount());
            dto.setStatus("SUCCESS");
            dto.setMessage("Transfer completed successfully");
            return dto;

        } catch (Exception e) {

            sendNotifications(
                    req.getSenderUserId(),
                    "Transaction Failed",
                    "Transfer failed due to system error"
            );

            TransactionDto dto = new TransactionDto();
            dto.setStatus("FAILED");
            dto.setMessage(e.getMessage());
            return dto;
        }
    }


    private void sendNotifications(String userId, String title, String message) {

        notificationClient.sendInApp(
                Map.of(
                        "userId", userId,
                        "title", title,
                        "message", message
                )
        );

        notificationClient.sendEmail(
                Map.of(
                        "userId", userId,
                        "subject", title,
                        "body", message
                )
        );
    }
}
