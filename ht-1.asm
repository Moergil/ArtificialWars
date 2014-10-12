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

; enabling noise
LDA #FLAG_NOISE
STA ADDR_FLG_S

LDA #5
STA $00

ROTATION:
	LDA #0
	STA $02
	LDA $00
	STA ADDR_ORD_ROTH

	LDA #SD_FR_LEFT
	ORA #SD_FR_RIGHT
	AND ADDR_DET_SEGM

	BEQ RANDOM_ROT

	LDA #SD_FR_LEFT
	AND ADDR_DET_SEGM

	BEQ TURN_RIGHT

	TURN_LEFT:
	LDA #250
	JMP STORE_ROT

	TURN_RIGHT:
	LDA #5

	STORE_ROT:
	STA $00
	LDA #10
	STA $01
	LDA #1
	STA $02
	JMP INITIATE_MOVE

	RANDOM_ROT:
	LDA ADDR_NOISE
	AND #%10001111
	STA $00
	LDA #20
	STA $01

	INITIATE_MOVE:
		LDA $01
		STA ADDR_ORD_MOVE
		JMP MOVING

	MOVING:
		LDA ADDR_FLAGS
		AND #FLAG_MOVING
		BNE MOVING

	LDA $02
	BEQ ROTATION ; dont shot if value at $02 is 0

	LDA #FLAG_ORD_FIRE
	STA ADDR_FLG_S

JMP ROTATION
END:
