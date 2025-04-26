# Informe de cobertura

La cobertura total de instrucciones del proyecto es del 47%, lo que indica que menos de la mitad del código ha sido ejecutado durante los tests. Además, la cobertura de ramas es aún más baja, con un 36%, lo que sugiere que muchas decisiones lógicas (if, switch, etc.) no han sido probadas completamente.

Algunas observaciones destacadas:

- El paquete service tiene la mejor cobertura (58% instrucciones, 92% ramas).
- Otros paquetes como model, configuration y controller.rest tienen baja cobertura ( en ocasiones incluso <40%), lo que podría ser un foco de mejora.
- La lógica compleja (Cxty) está poco cubierta: de 171 puntos de complejidad, solo 85 han sido ejecutados en tests. Lo que significa que esta no se han probado, y eso puede ocultar errores.

### ¿Qué clases/métodos crees que faltan por cubrir con pruebas? 

Empezando por el FilmRestController, nos faltan cubrir los metodos:
- addReview()
- deleteReview()

Del FilmWebController:
- addReview()
- removeReview()
- addFavorite()
- removeFavorite() 

En la clase FavoriteFilmService:
- addToFavorites()
- removeFromFavorites()

La clase FavoriteFilmService no ha sido cubierta ya que hace uso de la clase UserComponent que no está completamente implementada. En los tests donde se solicitaba verificar si se mantenían o eliminaban los usuarios que marcaron una película como favorita, preferimos añadir directamente los usuarios a la lista que esta en Film en lugar de depender del componente incompleto.

En ReviewService:
- addReview()
- deleteReview()

Las clases manejadoras de errores en web y rest; WebErrorHandler y RestErrorHandler

Finalmente, en la clase Review del modelo y la clase no se cubre nada. 

Aunque no se haya pedido explicitamente en los test del enuncuado, consideramos que la funcionalidad de añadir y eliminar reseñas es una parte básica y esencial de la aplicación. Por esto, cubrir la clase FilmService con pruebas seria importante para verificar que el sistema siempre actua de la forma esperada.

### ¿Qué clases/métodos crees que no hace falta cubrir con pruebas? 
En ciertas ocasiones encontramos que puede no ser necesario o imprescindible cubrir ciertas partes del codigo con pruebas. Discernimos estas situaciones en las cuales podemos decir que son prescindibles los test:

- Código trivial: Métodos simples como getters/setters.
- Código de bajo riesgo: Componentes estables que no cambian frecuentemente.
- Manejo de excepciones: Excepciones raras o altamente específicas que no requieren cobertura exhaustiva.

Consideramos poco importante cubrir las clases: 
- DatabaseInitilizer 

Y en cuando a los métodos, de la clase ImageUtils:
- remoteImageToBlob


