<!DOCTYPE html>
<html>
	<head>
		<title>Pokemon Orange</title>
		<script>window.CLASSROOM_CHAT_ENABLED = <#if chatEnabled>true<#else>false</#if>;</script>
		<script src="/js/lib/jquery.min.js"></script>
		<script src="/js/lib/js.cookie.min.js"></script>
		<script src="/js/lib/phaser.min.js"></script>
		<script src="/js/lib/easystar.min.js"></script>
		<script src="/js/lib/slick-ui.min.js"></script>
		<script src="/js/lib/phaser-input.min.js"></script>
		
		<style> 
			#main {
				display: flex;
				position: relative;
			}
			
			#main div {
				flex-grow: 0;
				flex-shrink: 0;
				lex-basis: 40px;
				align-items: center;
				justify-content: center;
				
			}
			
			.chat-msg {
				list-style-type: none;
				padding-top: 8px;
				padding-bottom: 8px;
				padding-left: 20px;
				padding-right: 20px;
				
			}
		</style>
	</head>
	<body style='background-color: grey; overflow: hidden;'>
		<div id="connection-status" hidden style="position:fixed;top:8px;left:50%;transform:translateX(-50%);z-index:100;background:#203040;color:white;padding:10px 16px;border-radius:8px;font:14px Arial"></div>
		<div id="main">
		
			<div style="margin-left: auto; visibility: hidden;"></div>
			<div id='game' style="">
				
			</div>
			
			<div style="width: 0px; visibility: hidden; height: 600px; width: 200px; background: lightgrey" id="chat-container">
				<ul id="chat" style="padding-left: 0px; height: 560px; margin-top: 0px; margin-bottom: 0px; overflow-y: auto;">
				</li>
				<input id="chat-input" placeholder="Chat" style="position: fixed; font-size:16px; margin: 5px; top: 567px; height: 25px; width: 286px">
			</div>
			
			<div style="margin-left: auto;"></div>
			
		</div>
		<!-- <script src="/socket.io/socket.io.js"></script> -->
		<script src="/js/lib/healthMeter.js"></script>
		<script src="/js/player_interaction.js"></script>
		<script src="/js/game.js"></script>
		<script src="/js/TradeUI.js"></script>
		<script src="/js/PurchasingUI.js"></script>
		<script src="/js/TeamManagerUI.js"></script>
		<script src="/js/utils.js"></script>
		<script src="/js/battle.js"></script>
		<script src="/js/login.js"></script>
		<script src="/js/sparseMap.js"></script>
		<script src="/js/animation.js"></script>
		<script src="/js/player.js"></script>
		<script src="/js/orange.js"></script>
		<script src="/js/net.js"></script>
		<script src="/js/home.js"></script>
		<script src="/js/main.js"></script>
		<script src="/js/websockets.js"></script>
		<script src="/js/general_datastructures.js"></script>
		<script src="/js/message_objects.js"></script>	
	<script>
		/*$(document).ready(function() {
		  setup_player_connection(); 
		});*/
	</script>
	</body>
</html>