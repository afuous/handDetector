import subprocess

def actionLeft(down):
    if down:
        subprocess.Popen(['xdotool', 'keydown', 'Left'])
    else:
        subprocess.Popen(['xdotool', 'keyup', 'Left'])

def actionRight(down):
    if down:
        subprocess.Popen(['xdotool', 'keydown', 'Right'])
    else:
        subprocess.Popen(['xdotool', 'keyup', 'Right'])
