package com.ecom.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserService userService;

	/**
	 * Corrected method for sending password reset emails.
	 * 
	 * @param url           The unique password reset URL.
	 * @param subject       The subject of the email.
	 * @param recipientEmail The email address of the user.
	 * @return True if the email was sent successfully, false otherwise.
	 */
	public Boolean sendMail(String url, String subject, String recipientEmail) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Enable multipart and set encoding

			helper.setFrom("daspabitra55@gmail.com", "Shopping Cart");
			helper.setTo(recipientEmail); // Use the correct recipient email
			helper.setSubject(subject);   // Use the subject passed to the method

			String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
					+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
					+ "\">Change my password</a></p>";
			
			helper.setText(content, true);
			mailSender.send(message);
			return true;
		} catch (MessagingException | UnsupportedEncodingException e) {
			// It's good practice to log the error
			e.printStackTrace();
			return false;
		}
	}

	public static String generateUrl(HttpServletRequest request) {
		String siteUrl = request.getRequestURL().toString();
		return siteUrl.replace(request.getServletPath(), "");
	}

	/**
	 * Corrected method for sending order confirmation emails.
	 * The 'msg' variable is now a local variable to prevent thread-safety issues.
	 * 
	 * @param order  The product order details.
	 * @param status The status of the order (e.g., "Confirmed", "Shipped").
	 * @return True if the email was sent successfully.
	 */
	public Boolean sendMailForProductOrder(ProductOrder order, String status) {
		// CRITICAL FIX: 'msg' is now a local variable, not a class field.
		String msg = "<p>Hello [[name]],</p>"
				+ "<p>Thank you, your order has been <b>[[orderStatus]]</b>.</p>"
				+ "<p><b>Product Details:</b></p>"
				+ "<p>Name : [[productName]]</p>"
				+ "<p>Category : [[category]]</p>"
				+ "<p>Quantity : [[quantity]]</p>"
				+ "<p>Price : [[price]]</p>"
				+ "<p>Payment Type : [[paymentType]]</p>";

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom("daspabitra55@gmail.com", "Shopping Cart");
			helper.setTo(order.getOrderAddress().getEmail());

			// Note: Assuming order.getProduct().getCategory() returns a String.
			// If it returns a Category object, you should use order.getProduct().getCategory().getName()
			msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
			msg = msg.replace("[[orderStatus]]", status);
			msg = msg.replace("[[productName]]", order.getProduct().getTitle());
			msg = msg.replace("[[category]]", order.getProduct().getCategory().toString()); // Using toString() for safety
			msg = msg.replace("[[quantity]]", order.getQuantity().toString());
			msg = msg.replace("[[price]]", order.getPrice().toString());
			msg = msg.replace("[[paymentType]]", order.getPaymentType());

			helper.setSubject("Product Order Status Update");
			helper.setText(msg, true);
			mailSender.send(message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		return userService.getUserByEmail(email);
	}
}