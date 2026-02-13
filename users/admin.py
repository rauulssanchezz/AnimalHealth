from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import User

@admin.register(User)
class MyUserAdmin(UserAdmin):
    # Esto añade el ID (UUID) y el email al listado
    list_display = ('email', 'username', 'is_staff', 'is_superuser')
    
    # Como tu login es por email, asegúrate de que el orden sea correcto
    ordering = ('email',)
    
    # Esto es necesario si añadiste campos extra a tu modelo User
    fieldsets = UserAdmin.fieldsets + (
        (None, {'fields': ('id',)}), # Por ejemplo para ver el UUID
    )
    readonly_fields = ('id',)
