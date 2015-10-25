window.onload = function() {
	function dgid(id) {
		return document.getElementById(id);
	}
	
	var canvas = dgid("canvas");
	var ctx = canvas.getContext("2d");
	
	document.oncontextmenu = function() {
		return false;
	};
	
	var keys = {};
	window.onkeydown = function(event) {
		if(!playing) return;
		var key = (event || window.event).keyCode;
		keys[key] = true;
	};
	window.onkeyup = function(event) {
		if(!playing) return;
		var key = (event || window.event).keyCode;
		keys[key] = false;
	};
	
	var game = {
		width: 600,
		height: 400,
		player: {
			width: 100,
			height: 10,
			speed: 3
		},
		ballSize: 20,
		ballSpeed: 2,
		tickTime: 10
	};
	
	var playing = false;
	var player = {
		width: 100,
		height: 10,
		speed: 3
	};
	player.location = (game.width - player.width) / 2;
	
	var ball;
	function repositionBall() {
		var angle;
		var rand = Math.random();
		if(rand < 0.25) angle = Math.PI / 6;
		else if(rand < 0.5) angle = Math.PI * 5 / 6;
		else if(rand < 0.75) angle = Math.PI * 7 / 6;
		else angle = Math.PI * 11 / 6;
		ball = {
			angle: angle + Math.random() / 2 - 0.25,
			size: 20,
			xSpeed: 3,
			ySpeed: 4,
			y: 0
		};
		ball.x = (game.width - ball.size) / 2;
	}
	repositionBall();
	
	dgid("play").onclick = function() {
		playing = true;
		dgid("lobby").style.display = "none";
		dgid("game").style.display = "block";
		canvas.width = game.width;
		canvas.height = game.height;
		setInterval(function() {
			if(keys[37] && !keys[39]) player.location -= game.player.speed;
			if(!keys[37] && keys[39]) player.location += game.player.speed;
			player.location = Math.min(Math.max(player.location, 0), game.width - player.width);
			var lastX = ball.x;
			var lastY = ball.y;
			ball.x += Math.cos(ball.angle) * ball.xSpeed;
			ball.y -= Math.sin(ball.angle) * ball.ySpeed;
			if(ball.y < 0) {
				ball.y = 0;
				ball.angle = -ball.angle;
			}
			else if(ball.y > game.height - player.height - ball.size) {
				if(ball.x + ball.size > player.location && ball.x < player.location + player.width) {
					ball.y = game.height - player.height - ball.size;
					ball.angle = -ball.angle;
				}
				else {
					repositionBall();
				}
			}
			else if(ball.x < 0) {
				ball.x = 0;
				ball.angle = Math.PI - ball.angle;
			}
			else if(ball.x > game.width - ball.size) {
				ball.x = game.width - ball.size;
				ball.angle = Math.PI - ball.angle;
			}
			draw();
		}, game.tickTime);
	};
	
	function draw() {
		ctx.fillStyle = "#70E0E0";
		ctx.fillRect(0, 0, game.width, game.height);
		ctx.fillStyle = "black";
		ctx.fillRect(player.location, game.height - game.player.height, game.player.width, game.player.height);
		ctx.fillRect(ball.x, ball.y, game.ballSize, game.ballSize);
	}
};