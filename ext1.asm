; $0000 is zeropage
; $0100 is stack
; $0100 code segment
; $04xx IO
;	 00 posX
;	 01 posY
;	 02 targetX
;	 03 targetY
;	 04 flags
;	 05 setFlags
;	 06 unsetFlags
;	 07 noise
;	 08 enemyX
;	 09 enemyY
; $04xx display
;	 10 display lobyte
;	 11 display lobyte

; Flags
; MOVE = 1
; SHOT = 2
; LOCK = 4
; MOVING = 8
; GUN_READY = 16

; setting up some constants for movement
;LDA #$01	; 1
;STA $40
;LDA #$FF	; -1
;STA $41

; check if robot is moving
;LDA $0404
;AND #$08
;BNE $10	; if moving, skip

;;;; moving ;;;
; adding offset to current X
;LDA $0400
;ADC $40
;STA $0402

; switch direction for next decision
;LDX $40
;LDA $41
;STX $41
;STA $40

; activate movement
;;LDA #$01
;;STA $0405
;;;; end moving ;;;;

.SEG PRG $0200
.SEG DAT $0300

LDA #$06	; set SHOT & LOCK flag
STA $0405	; save to IO

JMP $0200	; repeat
