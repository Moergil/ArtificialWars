; load X to value 10, used in loop
LDX #$0A
LDA #$01
STA $00
STA $01

FIBONACCI:
; setting value 1 to memory 0x0000 and 0x0001
LDA $00
ADC $01
STA $02

; algorithm
; loading value from 0x0000 to A
; adding with value from 0x0001
; storing result to 0x0002
LDA $01
STA $00
LDA $02
STA $01

; decrementing X
DEX

; if X is not 0, return to beginning
; in this scenario every instruction is 2 byte long, so 7*2 bytes backwards
; BNE $EF if you need constant for this scenario
BNE FIBONACCI

; loading result to A
LDA $02
STA $0410

; infinite loop
CLC
BCC $FD
