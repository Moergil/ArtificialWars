.SEG DAT $0300
.SEG PRG $0200

;LDA #10
;STA $00
;ADC $00

;LDA #10
;STA $FFFF
;ADC $FFFF

_NMI:
_IRQ:
	JMP INF ; todo, implementnut a odskusat RTI

_RES:
	LDA #10
ADD_L:
	ADC #1
	JMP ADD_L

INF:
	JMP INF
