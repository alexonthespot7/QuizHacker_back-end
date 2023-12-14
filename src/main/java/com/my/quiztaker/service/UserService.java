package com.my.quiztaker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.quiztaker.forms.Leaderboard;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	// The method returns the list of TOP-10 users in leaderboard (username,
	// rating). The position of current user is -1, what means the user is not in
	// the leaderboard
	public Leaderboard getLeaderboardNoAuth() {
		List<UserPublic> leaders = userRepository.findLeaderBoard();
		
		leaders = this.getTop10(leaders);

		Leaderboard leaderboard = new Leaderboard(leaders, -1);
		
		return leaderboard;
	}
	
	private List<UserPublic> getTop10(List<UserPublic> leaders) {
		if (leaders.size() > LIMIT) {
			leaders = leaders.subList(0, LIMIT);
		}
		
		return leaders;
	}
}
