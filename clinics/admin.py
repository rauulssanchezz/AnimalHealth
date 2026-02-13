from django.contrib import admin
from .models import Clinic, ClinicImage

# Esto permite editar las imágenes dentro de la propia página de la Clínica
class ClinicImageInline(admin.TabularInline):
    model = ClinicImage
    extra = 1  # Cuántos huecos vacíos para fotos nuevas quieres ver

@admin.register(Clinic)
class ClinicAdmin(admin.ModelAdmin):
    # Columnas que se verán en el listado principal
    list_display = ('name', 'address', 'latitude', 'longitude')
    
    # Buscador por nombre o dirección
    search_fields = ('name', 'address')
    
    # Metemos las fotos dentro de la vista de la clínica
    inlines = [ClinicImageInline]
