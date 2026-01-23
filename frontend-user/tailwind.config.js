/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#1989fa',
        success: '#07c160',
        danger: '#ee0a24',
        default: '#323233',
        gray: '#969799'
      },
      borderRadius: {
        'button': '25px',
        'card': '12px'
      }
    },
  },
  plugins: [],
}
