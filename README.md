# albo-test-1
Prueba técnica, parte 1

**Nota:** Es necesario contar con conexión a internet 

Este proyecto es una app para consola de Kotlin.
El proposito es generar un reporte sobre gastos e ingresos entre otros datos. Los datos a analzar se descargan mediante Ktor-client
desde el siguiente [json](https://gist.githubusercontent.com/astrocumbia/06ec83050ec79170b10a11d1d4924dfe/raw/ad791cddcff6df2ec424bfa3da7cdb86f266c57e/transactions.json)

El problema se aborda de manera sencilla, se realizan un primer sobre recorrido para analizar y recolectar los datos,
decidiendo sobre si se trata de un gasto o un ingreso, y agrupando dichos eventos por año-mes. Todo esto se almacena en un HashMap<año-mes, datos>

Déspues se realiza una segunda iteración sobre los datos generados de la primer iteración. Está vez solo se encarga de imprimir los datos de acuerdo al formato solicitado.
