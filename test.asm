.SEG PRG $0200
.SEG DAT $0300

BYTE RESULT
BYTE MSG "hello world!"
BYTE ED1 $01,$02,$FF
WORD IJUMP

ITER = $0A
RET2 = $FE

LDA #ITER
LOOP:
	INY
	SBC #$01
	BNE LOOP
LDA #$0A

JMP END

END:
LDA #$05
STA RESULT
LDA ED1
JMP END