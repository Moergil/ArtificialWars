.SEG PRG $0200
.SEG DAT $03F0

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

ADDR_DISPLAY = $0417

FLAG_MOVING = 1
FLAG_ROTATING = 2
FLAG_GUN_READY = 4
FLAG_DET_SEGM = 8
FLAG_DET_GRAD = 16
FLAG_ORD_FIRE = 32
FLAG_NOISE = 64
FLAG_COLLIDING = 128

SD_1 = 1
SD_2 = 2
SD_3 = 4
SD_4 = 8
SD_5 = 16
SD_6 = 32
SD_7 = 64
SD_8 = 128

SD_FRONT = 129
SD_BACK = 24

SD_FB = 153

SD_A_1 = $10
SD_A_2 = $30
SD_A_3 = $50
SD_A_4 = $70
SD_A_5 = $90
SD_A_6 = $B0
SD_A_7 = $D0
SD_A_8 = $F0

ROT_NONE = $00
ROT_RIGHT = $01
ROT_LEFT = $FF

; zeropage adresses
TMP = $00
LAST_GRADIENT_ADDR = $10
ROT_DIR_ADDR = $11
FLAGS_CACHE = $12
STATE_FLAGS = $13

; enabling noise
MOVE #FLAG_NOISE ADDR_FLG_S

;;;;; program ;;;;;

MAIN_LOOP:
	; cache IO flags for fast access
	MOVE ADDR_FLAGS FLAGS_CACHE

	AND #FLAG_COLLIDING
	BEQ CHECK_DANGER

	JSR SR_UNSTUCK
	JMP MAIN_LOOP

	CHECK_DANGER:
		ANDV FLAGS_CACHE #FLAG_DET_SEGM
		;BEQ FINDING_GRADIENT ; nothing is near, so we can search for targets
		BEQ MAIN_LOOP ; debug

		; something is quite near, so we will try to evade
		JSR SR_EVADE

	JMP MAIN_LOOP

	;;;; WIP ;;;;

	; continuosly rotate to one direction and check,
	; if gradient detector is activated
	FINDING_GRADIENT:
		; robot is actively following gradient (rotation is active)
		; so just jump to gradient adjustment subroutine
		ANDV ROT_DIR_ADDR #ROT_NONE
		BNE ADJUSTING_GRADIENT

		ANDV FLAGS_CACHE #FLAG_DET_GRAD
		BNE GRADIENT_FOUND ; gradient detector is activated, so we can start adjusting it
BRK
		; if gradient detector is not activated, just continuosly rotate in one direction
		MOVE #$10 ADDR_ORD_ROTH
		JMP MAIN_LOOP

	GRADIENT_FOUND:
		MOVE ADDR_DET_GRAD LAST_GRADIENT_ADDR

	ADJUSTING_GRADIENT:
		MOVE #ROT_RIGHT ROT_DIR_ADDR
		MOVE #$10 ADDR_ORD_ROTH
		;MOVE #FLAG_ORD_FIRE ADDR_FLG_S
		; TODO
		; do rotation for example per $10 steps
		; check often value of detector
		; if it starts to lessen, stop rotation
		; else jmp to main_loop to run checks (evade and such)
		;	keep rotation direction set, so logic can jump directly here
		; if gradient value is lost, clean everything and jump to MAIN_LOOP

	; jump to start
	JMP MAIN_LOOP






; subroutine for executing evade maneuver
; this will determine angle and move value to evade
; subroutine will return after maneuver is executed
SR_EVADE:
	; caching detection segment value
	MOVE ADDR_DET_SEGM TMP

	; macro for checking if segment is activated
	; and setting angle and move speed for escape from threat
	; if segment is excited, rotation value is saved to A and macro will jump
	; to evading loop init label
	MACRO DET_ROTATION SEGM ROT MOV SKIP_LBL
		LDA TMP
		AND #SD_%SEGM%
		BEQ %SKIP_LBL%

		LDA #%ROT%
		STA TMP
		LDA #%MOV%
		JMP EVADING_LOOP_INIT

		%SKIP_LBL%:
	/MACRO

	; checking segments
	; prioritized are front and back ones
	; as soon as some segment is excited,
	; program will jump to evading loop init label
	; parameters are relative rotation and movement direction
	DET_ROTATION 1 $A0 $D0 SKIP_1
	DET_ROTATION 8 $20 $D0 SKIP_2
	DET_ROTATION 4 $20 $4F SKIP_3
	DET_ROTATION 5 $A0 $4F SKIP_4
	DET_ROTATION 2 $90 $C0 SKIP_5
	DET_ROTATION 7 $10 $C0 SKIP_6
	DET_ROTATION 3 $10 $3F SKIP_7
	DET_ROTATION 6 $90 $3F SKIP_8

	EVADING_LOOP_INIT:
		; initiating movement and rotation
		STA ADDR_ORD_MOVE
		MOVE TMP ADDR_ORD_ROTH

		; saving moving and rotating flags for check
		LDA #FLAG_MOVING
		ORA #FLAG_ROTATING
		STA TMP

	EVADING_LOOP:
		ANDV ADDR_FLAGS TMP
		BNE EVADING_LOOP
	RTS

SR_UNSTUCK:
	MOVE #64 ADDR_ORD_ROTH
	MOVE #200 ADDR_ORD_MOVE

	UNSTUCK_SR_LOOP:
		ANDV ADDR_FLAGS #FLAG_MOVING
		BNE UNSTUCK_SR_LOOP

	RTS

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

MACRO ANDV OP1 OP2
	LDA %OP1%
	AND %OP2%
/MACRO

MACRO INF_WAIT
	INF_WAIT_LOOP:
	JMP INF_WAIT_LOOP
/MACRO

MACRO NEGA
	EOR #$FF
	SEC
	ADC #0
/MACRO
