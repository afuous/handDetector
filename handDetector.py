import cv2 as cv
import numpy as np

from actions import actionRight, actionLeft

# apparently colors are backward, so (255, 0, 0) is blue and (0, 0, 255) is red

def translucentRectangle(frame, topLeft, bottomRight, color, alpha):
    copy = frame.copy()
    cv.rectangle(copy, topLeft, bottomRight, color, cv.FILLED)
    cv.addWeighted(copy, alpha, frame, 1 - alpha, 0, frame)

def averageColor(frame, rect):
    topLeft = rect[0]
    bottomRight = rect[1]
    cropped = frame[topLeft[1]:bottomRight[1], topLeft[0]:bottomRight[0]]
    return cropped.mean(axis=0).mean(axis=0)

CALIBRATING_1 = 0 # left up, right down
CALIBRATING_2 = 1 # left down, right up
CALIBRATED = 2
state = CALIBRATING_1

leftColorNo = None
rightColorNo = None
leftColorYes = None
rightColorYes = None

leftOn = False
rightOn = False

capture = cv.VideoCapture(0)

while True:
    ret, frame = capture.read()
    frame = cv.flip(frame, 1)
    height, width, _ = frame.shape

    leftBox = ((0, 0), (200, 300))
    rightBox = ((width - 200, 0), (width, 300))

    # leftIndicator = ((0, height - 100), (100, height))
    # rightIndicator = ((width - 100, height - 100), (width, height))
    leftIndicator = ((0, 300), (200, height))
    rightIndicator = ((width - 200, 300), (width, height))

    cv.rectangle(frame, leftBox[0], leftBox[1], (0, 0, 0))
    cv.rectangle(frame, rightBox[0], rightBox[1], (0, 0, 0))

    if state != CALIBRATED:
        textOffset = 5
        fontSize = 1
        fontColor = (0, 0, 0)
        fontThickness = 1
        font = cv.FONT_HERSHEY_SIMPLEX

        text1 = "UP" if state == CALIBRATING_1 else "DOWN"
        text2 = "DOWN" if state == CALIBRATING_1 else "UP"

        w, h = cv.getTextSize(text1, font, fontSize, fontThickness)[0]
        cv.putText(frame,text1, (textOffset, textOffset + h), font, fontSize, fontColor, fontThickness)
        w, h = cv.getTextSize(text2, font, fontSize, fontThickness)[0]
        cv.putText(frame, text2, (width - w - textOffset, textOffset + h), font, fontSize, fontColor, fontThickness)

        middleText = "PRESS SPACE TO CALIBRATE"
        w, h = cv.getTextSize(middleText, font, fontSize/2, fontThickness)[0]
        cv.putText(frame, middleText, ((width - w)//2, textOffset + h), font, fontSize/2, fontColor, fontThickness)

    if state == CALIBRATED:
        avgLeft = averageColor(frame, leftBox)
        avgRight = averageColor(frame, rightBox)

        red = (0, 0, 255)
        alpha = 0.3
        threshold = 1

        if threshold * sum(abs(leftColorYes - avgLeft)) < sum(abs(leftColorNo - avgLeft)):
            translucentRectangle(frame, leftIndicator[0], leftIndicator[1], red, alpha)
            if not leftOn:
                actionLeft(True)
                leftOn = True
        else:
            if leftOn:
                actionLeft(False)
                leftOn = False

        if threshold * sum(abs(rightColorYes - avgRight)) < sum(abs(rightColorNo - avgRight)):
            translucentRectangle(frame, rightIndicator[0], rightIndicator[1], red, alpha)
            if not rightOn:
                actionRight(True)
                rightOn = True
        else:
            if rightOn:
                actionRight(False)
                rightOn = False

    cv.imshow("Hand Detector", frame)
    key = cv.waitKey(1) & 0xFF
    if key == ord("q"):
        break
    if key == ord(" "):
        if state == CALIBRATING_1:
            leftColorYes = averageColor(frame, leftBox)
            rightColorNo = averageColor(frame, rightBox)
            state = CALIBRATING_2
        elif state == CALIBRATING_2:
            leftColorNo = averageColor(frame, leftBox)
            rightColorYes = averageColor(frame, rightBox)
            state = CALIBRATED

capture.release()
cv.destroyAllWindows()
