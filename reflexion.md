# Reflexión — Quality & Governance Agent

Proyecto: citasalud-service-redo · commit `ad2598c` · veredicto: **BLOQUEADO**
(evidencia en `quality-output/verification.bloqueado.json`)

## ¿Qué cambió al dejar el veredicto en manos de un gate y no de mi propio criterio?

Normalmente doy algo por terminado con una mezcla de sensación y evidencia parcial:
veo que las pruebas pasan, leo el código y si se ve razonable, lo doy por bueno. Con
el gate esa sensación deja de valer nada. No le importa que el código se vea bien,
solo lee números: `passed == total`, cobertura ≥ 80%, cero críticas, cero secretos, y
cada criterio marcado `cumple`. No hay margen para "bueno, casi".

Eso se notó de inmediato en esta corrida. 13/13 pruebas, 97.2% de cobertura,
seguridad limpia — con eso yo ya habría dicho "aprobado" y seguido adelante. Pero al
cruzar cada FR del spec contra la prueba que supuestamente lo cubre, dos requisitos no
tenían realmente la prueba que decían tener, y otros tres ni siquiera estaban
modelados en el código (no hay horario de médico, no hay estado de "dado de baja").
El gate no me dejó redondear eso hacia arriba. Tuve que pasar de "esto se ve bien" a
"muéstrame la línea exacta que lo prueba", y aceptar que mi trabajo no es decidir si
pasa, sino juntar evidencia honesta para que otra cosa decida.

## ¿Qué pilar me costó más dejar en verde?

Criterios, por lejos. Pruebas y seguridad son casi mecánicos: corres la herramienta y
el número sale o no sale. Criterios en cambio te obliga a preguntarte si una prueba
*de verdad* prueba lo que el requisito pide, o si solo pasa por casualidad.

El ejemplo que más me hizo dudar fue FR-007 ("invitar a elegir otra franja"). Hay una
prueba que verifica que la reserva se rechaza con 409 y el código correcto — y la
tentación de marcarlo `cumple` fue real, porque el rechazo sí funciona. Pero el
requisito habla específicamente de invitar a elegir otra franja, y ninguna prueba
verifica eso. Ahí entendí la diferencia entre "el código no se rompe" y "el código
hace lo que el spec pide". Con FR-008 pasó algo distinto y más incómodo: el requisito
asume un concepto (horario de atención del médico) que el código nunca implementó. No
había ni siquiera una prueba mala que juzgar, había un vacío. Ahí el veredicto
correcto no es `incumple` ni `cumple`, es `desconocido` — y me costó aceptar que
"desconocido" es una respuesta válida y no una salida fácil.

## ¿Para qué me serviría esto en un equipo real?

Para frenar el momento en que alguien dice "las pruebas pasan, mándalo" sin haberse
preguntado si esas pruebas cubren de verdad el caso de error o la condición de
carrera. Si cada FR del spec necesita una prueba trazable para pasar el gate, el spec
deja de ser un documento que se escribe al inicio y se olvida, y se vuelve algo que
sigue siendo cierto en producción. Y como el umbral vive en código, nadie lo negocia
por una fecha de entrega.

El escaneo de seguridad automático aporta algo parecido pero en el terreno que casi
nadie revisa en cada PR: secretos sueltos, endpoints sin protección, dependencias con
vulnerabilidades conocidas. En esta misma corrida apareció la consola H2 abierta sin
restricción de perfil — algo que un scanner atrapa siempre y un review humano se salta
casi siempre porque no está mirando ese archivo ese día. Tener esto corriendo antes de
cada merge, en vez de como checklist que alguien puede saltarse con prisa, es lo que
de verdad cambiaría algo en un equipo real: pasar de "confío en que lo hiciste bien" a
"aquí está la evidencia, revísala tú mismo".
