.SEG PRG $0200
.SEG DAT $0300

MACRO PRINT V
	LDA V
	STA DISPLAY_ADDR
/MACRO

BYTE T $10,%10,010,10

OP1 = $00
OP2 = $01
RES = $02

COUNT = $0A

V1 = %00100000
V2 = 010
V3 = 10

DISPLAY_ADDR = $0410

LDA #V1
LDA #V2
LDA #V3

; load X to value 10, used in loop
LDX #COUNT
LDA #$01
STA OP1
STA OP2

FIBONACCI:
	; setting value 1 to memory 0x0000 and 0x0001
	LDA OP1
	ADC OP2
	STA RES

	; algorithm
	; loading value from 0x0000 to A
	; adding with value from 0x0001
	; storing result to 0x0002
	LDA OP2
	STA OP1
	LDA RES
	STA OP2

	; decrementing X
	DEX

	; if X is not 0, return to beginning
	; in this scenario every instruction is 2 byte long, so 7*2 bytes backwards
	; BNE $EF if you need constant for this scenario
	BNE FIBONACCI

; print result
PRINT RES

; infinite loop
END:
JMP END
