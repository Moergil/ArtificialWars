.SEG PRG $0200
.SEG DAT $0300

; IO $0400
;       00-16 MEXTIO
;       17	  Display

ADDR_ABS_ROTH = $0400
ADDR_ABS_ROTL = $0401
ADDR_ORD_ROTH = $0402
ADDR_ORD_ROTL = $0403
ADDR_ORD_MOVE = $0404
ADDR_DET_SEGM = $0405
ADDR_DET_GRAD = $0406
ADDR_FLAGS = $0407
ADDR_FLG_S = $0408
ADDR_FLG_U = $0409
ADDR_NOISE = $040A

FLAG_MOVING = 1
FLAG_ROTATING = 2
FLAG_GUN_READY = 4
FLAG_DET_SEGM = 8
FLAG_DET_GRAD = 16
FLAG_ORD_FIRE = 32
FLAG_NOISE = 64

SD_1 = 1
SD_2 = 2
SD_3 = 4
SD_4 = 8
SD_5 = 16
SD_6 = 32
SD_7 = 64
SD_8 = 128

SD_FR_LEFT = 128
SD_FR_RIGHT = 1

SD_LEFT_HALF = 15
SD_RIGHT_HALF = 240

ADDR_ROTATE_DIRECTION = $10

SKIP_BRANCH = 1
SKIP_RET = 1

; enabling noise
LDA #FLAG_NOISE
STA ADDR_FLG_S

LDA #5
STA $00

JORTS = 1

FAST_ROTATE_STEP = $10

TRACKING_SUB:
	; check if segment detector have some targets
	LDA ADDR_FLAGS
	AND #FLAG_DET_SEGM
	; just wait for targets if nothing is detected
	BEQ TRACKING_SUB

	; cache segment value for quicker access from zeropage
	MOVE ADDR_DET_SEGM $00

	; this macro will check if segment is excited
	; and if yes, it will store rotation value
	; to A and initiate rotation to that value
	MACRO DET_ROTATION SEGM ROT SKIP_LBL
		LDA $00
		AND #%SEGM%
		BEQ %SKIP_LBL%
		LDA #%ROT%
		JMP TRACKING_SUB_ROT_SET
		%SKIP_LBL%:
	/MACRO
	
	; checking from front segments to back ones
	DET_ROTATION SD_1 $10 SKIP_1
	DET_ROTATION SD_8 $F0 SKIP_2
	DET_ROTATION SD_2 $30 SKIP_3
	DET_ROTATION SD_7 $D0 SKIP_4
	DET_ROTATION SD_3 $50 SKIP_5
	DET_ROTATION SD_6 $B0 SKIP_6
	DET_ROTATION SD_4 $70 SKIP_7
	DET_ROTATION SD_5 $90 SKIP_8
	
	; no target found, so just loop again
	JMP TRACKING_SUB
	
	; rotate to excited segment direction
	; HACK also add some noise to precise rotation, more sofisticated later
	; HACK move a bit to the target, more sofisticated later
	TRACKING_SUB_ROT_SET:
		STA ADDR_ORD_ROTH
		LDA ADDR_NOISE
		STA ADDR_ORD_ROTL
		LDA #10
		STA ADDR_ORD_MOVE
	
	; wait for rotation end
	TRACKING_SUB_WHILE_ROT:
		LDA ADDR_FLAGS
		AND #FLAG_ROTATING
		BNE TRACKING_SUB_WHILE_ROT

	; suppose that we are now pointing to target,
	; so just fire one shot if gun is ready
	LDA #FLAG_ORD_FIRE
	STA ADDR_FLG_S

	JMP TRACKING_SUB

MACRO MOVE FROM TO
	LDA %FROM%
	STA %TO%
/MACRO

MACRO PUSH ADDR
	LDA %ADDR%
	PHA
/MACRO

MACRO POP ADDR
	PLA
	STA %ADDR%
/MACRO

MACRO INF_WAIT
	INF_WAIT_LOOP:
	JMP INF_WAIT_LOOP
/MACRO