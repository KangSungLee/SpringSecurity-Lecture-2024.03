package com.example.springSecurity.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.springSecurity.Service.SecurityUserService;
import com.example.springSecurity.entity.SecurityUser;
import com.example.springSecurity.util.ImageUtil;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class SecurityUserController {
	private final SecurityUserService securityService;
	private final BCryptPasswordEncoder bCryptEncoder;
	private final ImageUtil imageUtil;
	@Value("${spring.servlet.multipart.location}") private String uploadDir;

	@GetMapping("/login")
	public String login() {
		return "user/login";
	}
	
	@GetMapping("/register")
	public String registerForm() {
		return "user/register";
	}
	
	@PostMapping("/register")
	public String registerProc(String uid, String pwd, String pwd2, String uname,
			 String email, MultipartHttpServletRequest req, Model model) {
		String filename = null;
		MultipartFile filePart = req.getFile("picture");
		
		SecurityUser securityUser = securityService.getUserByUid(uid);
		if(securityUser != null) {
			model.addAttribute("msg", "사용자 ID가 중복되었습니다.");
			model.addAttribute("url", "/ss/user/register");
			return "common/alertMsg";
		}
		if (pwd == null || !pwd.equals(pwd2)) {
			model.addAttribute("msg", "패스워드 입력이 잘못되었습니다.");
			model.addAttribute("url", "/ss/user/register");
			return "common/alertMsg";
		}
		if (filePart.getContentType().contains("image")) {	// 파일이 들어와있는 모양(들어와 있다면)
			filename = filePart.getOriginalFilename();
			String path = uploadDir + "profile/" + filename;
			try {
				filePart.transferTo(new File(path));
			} catch (Exception e) {
				e.printStackTrace();
			}
			filename = imageUtil.squareImage(uid, filename);
		}
		String hashedPwd = bCryptEncoder.encode(pwd);
		securityUser = SecurityUser.builder()
				.uid(uid).pwd(hashedPwd).uname(uname).email(email).provider("ck wolrd")
				.picture("ss/file/download/profile/" + filename)
				.build();
		securityService.insertSecurityUser(securityUser);
		model.addAttribute("msg", "등록을 마쳤습니다. 로그인하세요");
		model.addAttribute("url", "/ss/user/login");	
		return "common/alertMsg";
	}
	
//	@ResponseBody
	@GetMapping("/loginSuccess")
	public String loginSuccess(HttpSession session) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// 세션에 현재 사용자 아이디
		String uid = authentication.getName();
		SecurityUser securityUser = securityService.getUserByUid(uid);
		session.setAttribute("sessUid", securityUser.getUid());
		session.setAttribute("sessUname", securityUser.getUname());
		
		return "redirect:/user/list/1";
//		return "loginSuccess - " + uid;
	}
	
	@GetMapping(value={"/list/{page}", "/list"})
	public String list(@PathVariable(required=false) Integer page, Model model, 
			HttpSession session) {	
		page = (page == null) ? 1 : page;
		int totalUserCount = securityService.getSecurityUserCount();
		int totalPages = (int) Math.ceil(totalUserCount / (double) securityService.COUNT_PER_PAGE);
		int startPage = (int) Math.ceil((page - 0.5) / securityService.COUNT_PER_PAGE - 1) * securityService.COUNT_PER_PAGE + 1;
		int endPage = Math.min(totalPages, startPage + securityService.COUNT_PER_PAGE - 1);
		List<Integer> pageList = new ArrayList<>();
		for (int i = startPage; i <= endPage; i++)
			pageList.add(i);

		session.setAttribute("currentUserPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageList", pageList);
		
		List<SecurityUser> list = securityService.getSecurityUserList(page);
		model.addAttribute("userList", list);
		return "user/list";
	}
}
