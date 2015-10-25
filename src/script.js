// This can be modified to do anything your heart desires; this is simply a demo

var robot = new java.awt.Robot();

function leftChange(raised) {
	if(raised) robot.keyPress(java.awt.event.KeyEvent.VK_LEFT);
	else robot.keyRelease(java.awt.event.KeyEvent.VK_LEFT);
}
function rightChange(raised) {
	if(raised) robot.keyPress(java.awt.event.KeyEvent.VK_RIGHT);
	else robot.keyRelease(java.awt.event.KeyEvent.VK_RIGHT);
}