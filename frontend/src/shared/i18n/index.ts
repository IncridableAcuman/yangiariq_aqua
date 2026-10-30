export type SupportedLanguage = 'uz' | 'ru' | 'en'

export const translations = {
  uz: {
    app: {
      title: 'Yangiariq Aqua AI',
      subtitle: 'Yangiariq tumani suv resurslarini oqilona boshqarish platformasi',
    },
    nav: {
      dashboard: 'Boshqaruv paneli',
      fields: 'Dala maydonlari',
      irrigation: 'Sugʻorish jurnali',
      problems: 'Suv muammolari',
      sensors: 'Datchik koʻrsatkichlari',
      aiAdvisor: 'AI Maslahatchi',
      settings: 'Sozlamalar',
    },
    common: {
      loading: 'Yuklanmoqda...',
      save: 'Saqlash',
      cancel: 'Bekor qilish',
      status: 'Holat',
      actions: 'Amallar',
      search: 'Qidiruv...',
      refresh: 'Yangilash',
    },
  },
  ru: {
    app: {
      title: 'Yangiariq Aqua AI',
      subtitle: 'Платформа интеллектуального управления водными ресурсами Янгиарыкского района',
    },
    nav: {
      dashboard: 'Панель управления',
      fields: 'Поля и участки',
      irrigation: 'Журнал полива',
      problems: 'Водные инциденты',
      sensors: 'Показания датчиков',
      aiAdvisor: 'ИИ Консультант',
      settings: 'Настройки',
    },
    common: {
      loading: 'Загрузка...',
      save: 'Сохранить',
      cancel: 'Отмена',
      status: 'Статус',
      actions: 'Действия',
      search: 'Поиск...',
      refresh: 'Обновить',
    },
  },
  en: {
    app: {
      title: 'Yangiariq Aqua AI',
      subtitle: 'Smart Water Decision Support Platform for Yangiariq District',
    },
    nav: {
      dashboard: 'Dashboard',
      fields: 'Fields',
      irrigation: 'Irrigation Records',
      problems: 'Water Incidents',
      sensors: 'Sensor Readings',
      aiAdvisor: 'AI Advisor',
      settings: 'Settings',
    },
    common: {
      loading: 'Loading...',
      save: 'Save',
      cancel: 'Cancel',
      status: 'Status',
      actions: 'Actions',
      search: 'Search...',
      refresh: 'Refresh',
    },
  },
} as const
